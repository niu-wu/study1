package com.example.study11.convert;

import com.example.study11.dao.EmployeeEducationDao;
import com.example.study11.dao.EmployeeEmergencyContactDao;
import com.example.study11.dao.EmployeeFamilyDao;
import com.example.study11.dao.EmployeeTrainingDao;
import com.example.study11.dao.EmployeeWorkHistoryDao;
import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.dto.EmployeeWorkHistoryItemDTO;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.enums.OnboardingStep;
import com.example.study11.entity.po.EmployeeEducationPo;
import com.example.study11.entity.po.EmployeeEmergencyContactPo;
import com.example.study11.entity.po.EmployeeFamilyPo;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.EmployeeTrainingPo;
import com.example.study11.entity.po.EmployeeWorkHistoryPo;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.manager.EmployeeOnboardingProgressCalculator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 把员工主档和一对多表组装成登记表响应，并附带进度。 */
@Component
public class EmployeeFormAssembler {

    private final EmployeeEducationDao employeeEducationDao;

    private final EmployeeWorkHistoryDao employeeWorkHistoryDao;

    private final EmployeeTrainingDao employeeTrainingDao;

    private final EmployeeFamilyDao employeeFamilyDao;

    private final EmployeeEmergencyContactDao employeeEmergencyContactDao;

    private final EmployeeOnboardingProgressCalculator progressCalculator;

    public EmployeeFormAssembler(EmployeeEducationDao employeeEducationDao,
                                 EmployeeWorkHistoryDao employeeWorkHistoryDao,
                                 EmployeeTrainingDao employeeTrainingDao,
                                 EmployeeFamilyDao employeeFamilyDao,
                                 EmployeeEmergencyContactDao employeeEmergencyContactDao,
                                 EmployeeOnboardingProgressCalculator progressCalculator) {
        this.employeeEducationDao = employeeEducationDao;
        this.employeeWorkHistoryDao = employeeWorkHistoryDao;
        this.employeeTrainingDao = employeeTrainingDao;
        this.employeeFamilyDao = employeeFamilyDao;
        this.employeeEmergencyContactDao = employeeEmergencyContactDao;
        this.progressCalculator = progressCalculator;
    }

    public EmployeeOnboardingFormVO toForm(EmployeePo source) {
        return toForm(source, true);
    }

    public EmployeeOnboardingFormVO toFormWithoutProgress(EmployeePo source) {
        return toForm(source, false);
    }

    private EmployeeOnboardingFormVO toForm(EmployeePo source, boolean includeProgress) {
        if (source == null) {
            return null;
        }
        EmployeeOnboardingFormVO result = mapEmployee(source);
        result.setEducations(toEducationItems(
                employeeEducationDao.selectByEmployeeUuid(source.getEmployeeUuid())));
        result.setWorkHistories(toWorkHistoryItems(
                employeeWorkHistoryDao.selectByEmployeeUuid(source.getEmployeeUuid())));
        result.setTrainings(toTrainingItems(
                employeeTrainingDao.selectByEmployeeUuid(source.getEmployeeUuid())));
        result.setFamilyMembers(toFamilyItems(
                employeeFamilyDao.selectByEmployeeUuid(source.getEmployeeUuid())));
        result.setEmergencyContacts(toEmergencyItems(
                employeeEmergencyContactDao.selectByEmployeeUuid(source.getEmployeeUuid())));
        if (includeProgress) {
            result.setProgress(progressCalculator.calculate(result));
        }
        return result;
    }

    public EmployeeOnboardingFormVO toSnapshot(EmployeePo employee, EmployeeOnboardingSaveRequest request) {
        EmployeeOnboardingFormVO result = mapEmployee(employee);
        if (request != null) {
            result.setEducations(safeList(request.getEducations()));
            result.setWorkHistories(safeList(request.getWorkHistories()));
            result.setTrainings(safeList(request.getTrainings()));
            result.setFamilyMembers(safeList(request.getFamilyMembers()));
            result.setEmergencyContacts(safeList(request.getEmergencyContacts()));
        }
        result.setProgress(progressCalculator.calculate(result));
        return result;
    }

    public void validateRequiredSteps(EmployeePo employee, EmployeeOnboardingSaveRequest request) {
        progressCalculator.validateRequiredSteps(toSnapshot(employee, request));
    }

    public void validateRequiredSteps(EmployeePo employee, EmployeeOnboardingSaveRequest request,
                                      Set<OnboardingStep> skipped) {
        progressCalculator.validateRequiredSteps(toSnapshot(employee, request), skipped);
    }

    private static EmployeeOnboardingFormVO mapEmployee(EmployeePo source) {
        EmployeeOnboardingFormVO result = new EmployeeOnboardingFormVO();
        result.setEmployeeUuid(source.getEmployeeUuid());
        result.setId(source.getId());
        result.setEmployeeNo(source.getEmployeeNo());
        result.setUserId(source.getUserId());
        result.setRecordUuid(source.getRecordUuid());
        EmploymentType employmentType = EmploymentType.fromCode(source.getEmploymentType());
        result.setEmploymentType(employmentType == null ? EmploymentType.FULL_TIME : employmentType);
        result.setFormStatus(EmployeeFormStatus.fromCode(source.getFormStatus()));
        result.setFullName(source.getFullName());
        result.setGender(source.getGender());
        result.setBirthDate(source.getBirthDate());
        result.setPosition(source.getPosition());
        result.setPhone(source.getPhone());
        result.setEmail(source.getEmail());
        result.setIdCard(source.getIdCard());
        result.setMaritalStatus(source.getMaritalStatus());
        result.setPoliticalStatus(source.getPoliticalStatus());
        result.setNationality(source.getNationality());
        result.setEthnicity(source.getEthnicity());
        result.setNativePlace(source.getNativePlace());
        result.setHukouLocation(source.getHukouLocation());
        result.setCurrentAddress(source.getCurrentAddress());
        result.setPostalCode(source.getPostalCode());
        result.setHealthStatus(source.getHealthStatus());
        result.setHighestEducation(source.getHighestEducation());
        result.setMajor(source.getMajor());
        result.setProfessionalTitle(source.getProfessionalTitle());
        result.setForeignLanguage(source.getForeignLanguage());
        result.setHobbies(source.getHobbies());
        result.setPhotoPath(source.getPhotoPath());
        result.setPhotoUploaded(source.getPhotoPath() != null && !source.getPhotoPath().isBlank());
        result.setWechatAccount(source.getWechatAccount());
        result.setWechatOpenid(source.getWechatOpenid());
        result.setWechatUnionid(source.getWechatUnionid());
        result.setWechatBoundAt(source.getWechatBoundAt());
        result.setSubmittedAt(source.getSubmittedAt());
        result.setHrConfirmedAt(source.getHrConfirmedAt());
        result.setHrConfirmedBy(source.getHrConfirmedBy());
        result.setCreatedAt(source.getCreatedAt());
        result.setUpdatedAt(source.getUpdatedAt());
        return result;
    }

    private static List<EmployeeEducationItemDTO> toEducationItems(List<EmployeeEducationPo> source) {
        List<EmployeeEducationItemDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (EmployeeEducationPo item : source) {
            EmployeeEducationItemDTO dto = new EmployeeEducationItemDTO();
            dto.setSortNo(item.getSortNo());
            dto.setStartDate(item.getStartDate());
            dto.setEndDate(item.getEndDate());
            dto.setSchoolName(item.getSchoolName());
            dto.setMajor(item.getMajor());
            dto.setEducationLevel(item.getEducationLevel());
            dto.setCertificate(item.getCertificate());
            result.add(dto);
        }
        return result;
    }

    private static List<EmployeeWorkHistoryItemDTO> toWorkHistoryItems(List<EmployeeWorkHistoryPo> source) {
        List<EmployeeWorkHistoryItemDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (EmployeeWorkHistoryPo item : source) {
            EmployeeWorkHistoryItemDTO dto = new EmployeeWorkHistoryItemDTO();
            dto.setSortNo(item.getSortNo());
            dto.setStartDate(item.getStartDate());
            dto.setEndDate(item.getEndDate());
            dto.setCompanyName(item.getCompanyName());
            dto.setPosition(item.getPosition());
            dto.setLeaveReason(item.getLeaveReason());
            dto.setReferenceName(item.getReferenceName());
            dto.setReferencePhone(item.getReferencePhone());
            result.add(dto);
        }
        return result;
    }

    private static List<EmployeeTrainingItemDTO> toTrainingItems(List<EmployeeTrainingPo> source) {
        List<EmployeeTrainingItemDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (EmployeeTrainingPo item : source) {
            EmployeeTrainingItemDTO dto = new EmployeeTrainingItemDTO();
            dto.setSortNo(item.getSortNo());
            dto.setStartDate(item.getStartDate());
            dto.setEndDate(item.getEndDate());
            dto.setInstitution(item.getInstitution());
            dto.setCourseContent(item.getCourseContent());
            dto.setTrainingResult(item.getTrainingResult());
            result.add(dto);
        }
        return result;
    }

    private static List<EmployeeFamilyItemDTO> toFamilyItems(List<EmployeeFamilyPo> source) {
        List<EmployeeFamilyItemDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (EmployeeFamilyPo item : source) {
            EmployeeFamilyItemDTO dto = new EmployeeFamilyItemDTO();
            dto.setSortNo(item.getSortNo());
            dto.setFullName(item.getFullName());
            dto.setRelationship(item.getRelationship());
            dto.setWorkUnit(item.getWorkUnit());
            dto.setJobTitle(item.getJobTitle());
            result.add(dto);
        }
        return result;
    }

    private static List<EmployeeEmergencyContactItemDTO> toEmergencyItems(
            List<EmployeeEmergencyContactPo> source) {
        List<EmployeeEmergencyContactItemDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (EmployeeEmergencyContactPo item : source) {
            EmployeeEmergencyContactItemDTO dto = new EmployeeEmergencyContactItemDTO();
            dto.setSortNo(item.getSortNo());
            dto.setFullName(item.getFullName());
            dto.setRelationship(item.getRelationship());
            dto.setAddress(item.getAddress());
            dto.setPostalCode(item.getPostalCode());
            dto.setPhone(item.getPhone());
            result.add(dto);
        }
        return result;
    }

    private static <T> List<T> safeList(List<T> source) {
        return source == null ? new ArrayList<>() : source;
    }
}
