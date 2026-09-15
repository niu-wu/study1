package com.example.study11.service.impl;

import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.dao.EmployeeEducationDao;
import com.example.study11.dao.EmployeeEmergencyContactDao;
import com.example.study11.dao.EmployeeFamilyDao;
import com.example.study11.dao.EmployeeTrainingDao;
import com.example.study11.dao.EmployeeWorkHistoryDao;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.vo.EmployeeAssignmentConfirmVO;
import com.example.study11.entity.vo.EmployeeAssignmentListItemVO;
import com.example.study11.exception.ApiException;
import com.example.study11.manager.EmployeeOnboardingProgressCalculator;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.service.RoleAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAssignmentServiceImplTest {

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Mock
    private EmployeeDao employeeDao;

    @Mock
    private EmployeeEducationDao employeeEducationDao;

    @Mock
    private EmployeeWorkHistoryDao employeeWorkHistoryDao;

    @Mock
    private EmployeeTrainingDao employeeTrainingDao;

    @Mock
    private EmployeeFamilyDao employeeFamilyDao;

    @Mock
    private EmployeeEmergencyContactDao employeeEmergencyContactDao;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @Mock
    private EmployeePhotoStorageService employeePhotoStorageService;

    private EmployeeAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        EmployeeFormAssembler assembler = new EmployeeFormAssembler(
                employeeEducationDao, employeeWorkHistoryDao, employeeTrainingDao,
                employeeFamilyDao, employeeEmergencyContactDao,
                new EmployeeOnboardingProgressCalculator());
        service = new EmployeeAssignmentServiceImpl(
                employeeDao, roleAuthorizationService, employeePhotoStorageService, assembler);
    }

    @Test
    void listPendingReturnsSubmittedFullTimeEmployees() {
        EmployeePo employee = submittedFullTime();
        when(employeeDao.selectPendingAssignments()).thenReturn(List.of(employee));

        List<EmployeeAssignmentListItemVO> result = service.listPending(7);

        assertEquals(1, result.size());
        assertEquals(EMPLOYEE_UUID, result.get(0).getEmployeeUuid());
        assertEquals("张三", result.get(0).getFullName());
        assertEquals(EmployeeFormStatus.SUBMITTED, result.get(0).getFormStatus());
        verify(roleAuthorizationService).requireHrOrAdmin(7);
    }

    @Test
    void confirmMarksHrConfirmedAndReturnsSuccessMessage() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(submittedFullTime());
        when(employeeDao.confirmAssignment(eq(EMPLOYEE_UUID), any(LocalDateTime.class), eq(7))).thenReturn(1);

        EmployeeAssignmentConfirmVO result = service.confirm(EMPLOYEE_UUID, 7);

        assertEquals(EMPLOYEE_UUID, result.getEmployeeUuid());
        assertEquals("张三 入职成功", result.getMessage());
        verify(employeeDao).confirmAssignment(eq(EMPLOYEE_UUID), any(LocalDateTime.class), eq(7));
    }

    @Test
    void confirmRejectsPartTimeEmployee() {
        EmployeePo partTime = submittedFullTime();
        partTime.setEmploymentType(EmploymentType.PART_TIME.getCode());
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(partTime);

        ApiException exception = assertThrows(ApiException.class, () -> service.confirm(EMPLOYEE_UUID, 7));

        assertEquals(422, exception.getStatus().value());
        verify(employeeDao, never()).confirmAssignment(any(), any(), any());
    }

    @Test
    void confirmRejectsDraftForm() {
        EmployeePo draft = submittedFullTime();
        draft.setFormStatus(EmployeeFormStatus.DRAFT.getCode());
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(draft);

        ApiException exception = assertThrows(ApiException.class, () -> service.confirm(EMPLOYEE_UUID, 7));

        assertEquals(422, exception.getStatus().value());
        assertEquals("员工尚未提交登记表", exception.getMessage());
    }

    @Test
    void confirmRejectsAlreadyConfirmedEmployee() {
        EmployeePo confirmed = submittedFullTime();
        confirmed.setHrConfirmedAt(LocalDateTime.of(2026, 9, 14, 10, 0));
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmed);

        ApiException exception = assertThrows(ApiException.class, () -> service.confirm(EMPLOYEE_UUID, 7));

        assertEquals(409, exception.getStatus().value());
    }

    @Test
    void listPendingRejectsUserRole() {
        doThrow(ApiException.forbidden("无权执行此操作")).when(roleAuthorizationService).requireHrOrAdmin(51);

        ApiException exception = assertThrows(ApiException.class, () -> service.listPending(51));

        assertEquals(403, exception.getStatus().value());
        verify(employeeDao, never()).selectPendingAssignments();
    }

    @Test
    void getDetailRejectsMissingEmployee() {
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> service.getDetail(EMPLOYEE_UUID, 7));

        assertEquals(404, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("员工档案不存在"));
    }

    private static EmployeePo submittedFullTime() {
        EmployeePo employee = new EmployeePo();
        employee.setEmployeeUuid(EMPLOYEE_UUID);
        employee.setEmployeeNo("EMP7C9E66797425");
        employee.setFullName("张三");
        employee.setPhone("13800138000");
        employee.setPosition("Java开发");
        employee.setEmploymentType(EmploymentType.FULL_TIME.getCode());
        employee.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        employee.setSubmittedAt(LocalDateTime.of(2026, 9, 14, 9, 0));
        return employee;
    }
}
