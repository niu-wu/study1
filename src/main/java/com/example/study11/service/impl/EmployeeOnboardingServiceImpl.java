package com.example.study11.service.impl;

import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.dao.OnboardingRecordDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.WorkLocation;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.OnboardingRecordPo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.exception.ApiException;
import com.example.study11.manager.EmployeeChildPersistence;
import com.example.study11.service.EmployeeOnboardingService;
import com.example.study11.service.EmployeePhotoStorageService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** 员工入职登记实现。草稿和提交共用同一行，提交后锁定。 */
@Service
public class EmployeeOnboardingServiceImpl implements EmployeeOnboardingService {

    private final EmployeeDao employeeDao;

    private final OnboardingRecordDao onboardingRecordDao;

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final UserDao userDao;

    private final EmployeePhotoStorageService employeePhotoStorageService;

    private final EmployeeChildPersistence childPersistence;

    private final EmployeeFormAssembler formAssembler;

    public EmployeeOnboardingServiceImpl(EmployeeDao employeeDao,
                                         OnboardingRecordDao onboardingRecordDao,
                                         RecruitmentInfoDao recruitmentInfoDao,
                                         UserDao userDao,
                                         EmployeePhotoStorageService employeePhotoStorageService,
                                         EmployeeChildPersistence childPersistence,
                                         EmployeeFormAssembler formAssembler) {
        this.employeeDao = employeeDao;
        this.onboardingRecordDao = onboardingRecordDao;
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.userDao = userDao;
        this.employeePhotoStorageService = employeePhotoStorageService;
        this.childPersistence = childPersistence;
        this.formAssembler = formAssembler;
    }

    @Override
    @Transactional
    public EmployeeOnboardingFormVO getMyForm(Integer userId) {
        validateUserId(userId);
        EmployeePo employee = employeeDao.selectByUserId(userId);
        if (employee == null) {
            employee = createDraft(userId);
        }
        return formAssembler.toForm(employee);
    }

    @Override
    @Transactional
    public EmployeeOnboardingFormVO saveDraft(Integer userId, EmployeeOnboardingSaveRequest request) {
        EmployeePo employee = lockDraft(userId);
        EmployeeOnboardingSaveRequest body = requireRequest(request);
        applyRequest(employee, body);
        persistDraft(employee, body);
        return reloadForm(employee.getEmployeeUuid());
    }

    @Override
    @Transactional
    public EmployeeOnboardingFormVO submit(Integer userId, EmployeeOnboardingSaveRequest request) {
        EmployeePo employee = lockDraft(userId);
        EmployeeOnboardingSaveRequest body = requireRequest(request);
        applyRequest(employee, body);
        formAssembler.validateRequiredSteps(employee, body);
        LocalDateTime now = LocalDateTime.now();
        employee.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        employee.setSubmittedAt(now);
        employee.setUpdatedAt(now);
        persistDraft(employee, body);
        return reloadForm(employee.getEmployeeUuid());
    }

    @Override
    @Transactional
    public EmployeeOnboardingFormVO uploadPhoto(Integer userId, MultipartFile file) {
        EmployeePo employee = lockDraft(userId);
        String storedFilename = employeePhotoStorageService.store(file);
        String previousPhoto = employee.getPhotoPath();
        employee.setPhotoPath(storedFilename);
        employee.setUpdatedAt(LocalDateTime.now());
        try {
            if (employeeDao.updateByEmployeeUuid(employee) != 1) {
                employeePhotoStorageService.deleteQuietly(storedFilename);
                throw ApiException.internalServerError("员工登记表保存失败");
            }
        } catch (DuplicateKeyException exception) {
            employeePhotoStorageService.deleteQuietly(storedFilename);
            throw translateDuplicate(exception);
        }
        if (previousPhoto != null && !previousPhoto.equals(storedFilename)) {
            employeePhotoStorageService.deleteQuietly(previousPhoto);
        }
        return reloadForm(employee.getEmployeeUuid());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePhotoFileVO loadMyPhoto(Integer userId) {
        validateUserId(userId);
        EmployeePo employee = employeeDao.selectByUserId(userId);
        if (employee == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        return employeePhotoStorageService.load(employee.getPhotoPath());
    }

    private EmployeePo lockDraft(Integer userId) {
        validateUserId(userId);
        EmployeePo employee = employeeDao.selectByUserIdForUpdate(userId);
        if (employee == null) {
            createDraft(userId);
            employee = employeeDao.selectByUserIdForUpdate(userId);
        }
        if (employee == null) {
            throw ApiException.internalServerError("员工登记表创建失败");
        }
        EmployeeFormStatus status = EmployeeFormStatus.fromCode(employee.getFormStatus());
        if (status == EmployeeFormStatus.SUBMITTED) {
            throw ApiException.unprocessableEntity("登记表已提交，不能再修改");
        }
        if (status != EmployeeFormStatus.DRAFT) {
            throw ApiException.internalServerError("登记表状态无效");
        }
        return employee;
    }

    private EmployeePo createDraft(Integer userId) {
        OnboardingRecordPo onboarding = onboardingRecordDao.selectByUserId(userId);
        if (onboarding == null) {
            throw ApiException.unprocessableEntity("尚未办理入职，无法填写登记表");
        }
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuid(onboarding.getRecordUuid());
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        if (parseStatus(recruitment) != RecruitmentStatus.ONBOARDED) {
            throw ApiException.unprocessableEntity("当前招聘状态不允许填写入职登记表");
        }
        String fullName = trimToNull(recruitment.getApplicantName());
        if (fullName == null) {
            throw ApiException.unprocessableEntity("招聘记录缺少姓名，无法预填登记表");
        }

        LocalDateTime now = LocalDateTime.now();
        String employeeUuid = UUID.randomUUID().toString();
        EmployeePo employee = new EmployeePo();
        employee.setEmployeeUuid(employeeUuid);
        employee.setEmployeeNo(generateEmployeeNo(employeeUuid));
        employee.setUserId(userId);
        employee.setRecordUuid(onboarding.getRecordUuid());
        employee.setEmploymentType(EmploymentType.FULL_TIME.getCode());
        employee.setEmploymentStatus(EmploymentStatus.PROBATION.getCode());
        employee.setWorkLocation(WorkLocation.HEADQUARTERS.getCode());
        employee.setHiredAt(onboarding.getOnboardingDate());
        employee.setFullName(fullName);
        employee.setGender(trimToNull(recruitment.getGender()));
        employee.setBirthDate(readBirthday(userDao.selectUserById(userId)));
        employee.setPosition(trimToNull(recruitment.getPosition()));
        employee.setPhone(trimToNull(recruitment.getPhone()));
        employee.setEmail(trimToNull(recruitment.getEmail()));
        employee.setFormStatus(EmployeeFormStatus.DRAFT.getCode());
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        try {
            if (employeeDao.insert(employee) != 1) {
                throw ApiException.internalServerError("员工登记表保存失败");
            }
        } catch (DuplicateKeyException exception) {
            EmployeePo existing = employeeDao.selectByUserId(userId);
            if (existing != null) {
                return existing;
            }
            throw translateDuplicate(exception);
        }
        return employee;
    }

    private void persistDraft(EmployeePo employee, EmployeeOnboardingSaveRequest request) {
        try {
            childPersistence.replaceAll(employee.getEmployeeUuid(), request, employee.getUpdatedAt());
            if (employeeDao.updateByEmployeeUuid(employee) != 1) {
                throw ApiException.internalServerError("员工登记表保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw translateDuplicate(exception);
        }
    }

    private void applyRequest(EmployeePo employee, EmployeeOnboardingSaveRequest request) {
        String fullName = trimToNull(request.getFullName());
        if (fullName != null) {
            employee.setFullName(fullName);
        }
        employee.setGender(trimToNull(request.getGender()));
        employee.setBirthDate(request.getBirthDate());
        employee.setPosition(trimToNull(request.getPosition()));
        employee.setPhone(trimToNull(request.getPhone()));
        employee.setEmail(trimToNull(request.getEmail()));
        employee.setIdCard(trimToNull(request.getIdCard()));
        employee.setMaritalStatus(trimToNull(request.getMaritalStatus()));
        employee.setPoliticalStatus(trimToNull(request.getPoliticalStatus()));
        employee.setNationality(trimToNull(request.getNationality()));
        employee.setEthnicity(trimToNull(request.getEthnicity()));
        employee.setNativePlace(trimToNull(request.getNativePlace()));
        employee.setHukouLocation(trimToNull(request.getHukouLocation()));
        employee.setCurrentAddress(trimToNull(request.getCurrentAddress()));
        employee.setPostalCode(trimToNull(request.getPostalCode()));
        employee.setHealthStatus(trimToNull(request.getHealthStatus()));
        employee.setHighestEducation(trimToNull(request.getHighestEducation()));
        employee.setMajor(trimToNull(request.getMajor()));
        employee.setProfessionalTitle(trimToNull(request.getProfessionalTitle()));
        employee.setForeignLanguage(trimToNull(request.getForeignLanguage()));
        employee.setHobbies(trimToNull(request.getHobbies()));
        employee.setWechatAccount(trimToNull(request.getWechatAccount()));
        employee.setUpdatedAt(LocalDateTime.now());
    }

    private EmployeeOnboardingFormVO reloadForm(String employeeUuid) {
        EmployeePo saved = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (saved == null) {
            throw ApiException.internalServerError("员工登记表保存失败");
        }
        return formAssembler.toForm(saved);
    }

    private static EmployeeOnboardingSaveRequest requireRequest(EmployeeOnboardingSaveRequest request) {
        if (request == null) {
            throw ApiException.badRequest("登记表内容不能为空");
        }
        return request;
    }

    private static RecruitmentStatus parseStatus(RecruitmentInfoPo recruitment) {
        RecruitmentStatus status = RecruitmentStatus.fromCode(recruitment.getStatus());
        if (status == null) {
            throw ApiException.internalServerError("招聘记录状态无效");
        }
        return status;
    }

    private static LocalDate readBirthday(UserPo user) {
        if (user == null || user.getBirthday() == null) {
            return null;
        }
        java.util.Date birthday = user.getBirthday();
        if (birthday instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return new Date(birthday.getTime()).toLocalDate();
    }

    private static String generateEmployeeNo(String employeeUuid) {
        return "EMP" + employeeUuid.replace("-", "").substring(0, 12).toUpperCase();
    }

    private static ApiException translateDuplicate(DuplicateKeyException exception) {
        String message = String.valueOf(exception.getMessage());
        if (message.contains("uk_employee_id_card")) {
            return ApiException.conflict("身份证号已被使用");
        }
        if (message.contains("uk_employee_user_id") || message.contains("uk_employee_record_uuid")) {
            return ApiException.conflict("该入职记录已存在员工档案");
        }
        if (message.contains("uk_employee_no")) {
            return ApiException.conflict("工号冲突，请重试");
        }
        return ApiException.conflict("员工档案保存冲突");
    }

    private static void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw ApiException.unauthorized("当前登录用户无效");
        }
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
