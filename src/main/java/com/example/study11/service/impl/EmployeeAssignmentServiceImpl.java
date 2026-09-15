package com.example.study11.service.impl;

import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.vo.EmployeeAssignmentConfirmVO;
import com.example.study11.entity.vo.EmployeeAssignmentListItemVO;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.EmployeeAssignmentService;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.service.RoleAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 人员分配：HR 审核正式员工已提交的登记表。 */
@Service
public class EmployeeAssignmentServiceImpl implements EmployeeAssignmentService {

    private final EmployeeDao employeeDao;

    private final RoleAuthorizationService roleAuthorizationService;

    private final EmployeePhotoStorageService employeePhotoStorageService;

    private final EmployeeFormAssembler formAssembler;

    public EmployeeAssignmentServiceImpl(EmployeeDao employeeDao,
                                         RoleAuthorizationService roleAuthorizationService,
                                         EmployeePhotoStorageService employeePhotoStorageService,
                                         EmployeeFormAssembler formAssembler) {
        this.employeeDao = employeeDao;
        this.roleAuthorizationService = roleAuthorizationService;
        this.employeePhotoStorageService = employeePhotoStorageService;
        this.formAssembler = formAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAssignmentListItemVO> listPending(Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        List<EmployeePo> employees = employeeDao.selectPendingAssignments();
        List<EmployeeAssignmentListItemVO> result = new ArrayList<>();
        if (employees == null) {
            return result;
        }
        for (EmployeePo employee : employees) {
            EmployeeOnboardingFormVO form = formAssembler.toForm(employee);
            EmployeeAssignmentListItemVO item = new EmployeeAssignmentListItemVO();
            item.setEmployeeUuid(employee.getEmployeeUuid());
            item.setEmployeeNo(employee.getEmployeeNo());
            item.setFullName(employee.getFullName());
            item.setPhone(employee.getPhone());
            item.setPosition(employee.getPosition());
            item.setFormStatus(EmployeeFormStatus.fromCode(employee.getFormStatus()));
            item.setSubmittedAt(employee.getSubmittedAt());
            item.setPercent(form.getProgress() == null ? 0 : form.getProgress().getPercent());
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOnboardingFormVO getDetail(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        return formAssembler.toForm(requireFullTimeEmployee(employeeUuid));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePhotoFileVO loadPhoto(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requireFullTimeEmployee(employeeUuid);
        return employeePhotoStorageService.load(employee.getPhotoPath());
    }

    @Override
    @Transactional
    public EmployeeAssignmentConfirmVO confirm(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        validateEmployeeUuid(employeeUuid);
        EmployeePo employee = employeeDao.selectByEmployeeUuidForUpdate(employeeUuid);
        if (employee == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        if (EmploymentType.PART_TIME.getCode().equals(employee.getEmploymentType())) {
            throw ApiException.unprocessableEntity("兼职人员无需人员分配");
        }
        EmployeeFormStatus status = EmployeeFormStatus.fromCode(employee.getFormStatus());
        if (status != EmployeeFormStatus.SUBMITTED) {
            throw ApiException.unprocessableEntity("员工尚未提交登记表");
        }
        if (employee.getHrConfirmedAt() != null) {
            throw ApiException.conflict("该员工已办理入职");
        }
        LocalDateTime now = LocalDateTime.now();
        if (employeeDao.confirmAssignment(employeeUuid, now, operatorUserId) != 1) {
            throw ApiException.conflict("该员工已办理入职");
        }
        String fullName = employee.getFullName() == null ? "" : employee.getFullName();
        EmployeeAssignmentConfirmVO result = new EmployeeAssignmentConfirmVO();
        result.setEmployeeUuid(employeeUuid);
        result.setFullName(fullName);
        result.setMessage(fullName + " 入职成功");
        result.setHrConfirmedAt(now);
        return result;
    }

    private EmployeePo requireFullTimeEmployee(String employeeUuid) {
        validateEmployeeUuid(employeeUuid);
        EmployeePo employee = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (employee == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        if (EmploymentType.PART_TIME.getCode().equals(employee.getEmploymentType())) {
            throw ApiException.unprocessableEntity("兼职人员无需人员分配");
        }
        return employee;
    }

    private static void validateEmployeeUuid(String employeeUuid) {
        if (employeeUuid == null || employeeUuid.isBlank()) {
            throw ApiException.badRequest("员工档案 UUID 不能为空");
        }
        try {
            UUID.fromString(employeeUuid);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("员工档案 UUID 格式不正确");
        }
    }
}
