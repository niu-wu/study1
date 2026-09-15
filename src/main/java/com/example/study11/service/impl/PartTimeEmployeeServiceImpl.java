package com.example.study11.service.impl;

import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.enums.OnboardingStep;
import com.example.study11.entity.enums.WorkLocation;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.PartTimeEmployeeCreateVO;
import com.example.study11.exception.ApiException;
import com.example.study11.manager.EmployeeChildPersistence;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.service.PartTimeEmployeeService;
import com.example.study11.service.RoleAuthorizationService;
import com.example.study11.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/** 兼职人员登记：创建可登录账号并写入完整员工档案。 */
@Service
public class PartTimeEmployeeServiceImpl implements PartTimeEmployeeService {

    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";

    private static final String DIGITS = "23456789";

    private static final String PASSWORD_ALPHABET = UPPERCASE + LOWERCASE + DIGITS;

    private static final int INITIAL_PASSWORD_LENGTH = 8;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmployeeDao employeeDao;

    private final UserService userService;

    private final RoleAuthorizationService roleAuthorizationService;

    private final EmployeePhotoStorageService employeePhotoStorageService;

    private final EmployeeChildPersistence childPersistence;

    private final EmployeeFormAssembler formAssembler;

    private final Clock clock;

    public PartTimeEmployeeServiceImpl(EmployeeDao employeeDao,
                                       UserService userService,
                                       RoleAuthorizationService roleAuthorizationService,
                                       EmployeePhotoStorageService employeePhotoStorageService,
                                       EmployeeChildPersistence childPersistence,
                                       EmployeeFormAssembler formAssembler,
                                       Clock clock) {
        this.employeeDao = employeeDao;
        this.userService = userService;
        this.roleAuthorizationService = roleAuthorizationService;
        this.employeePhotoStorageService = employeePhotoStorageService;
        this.childPersistence = childPersistence;
        this.formAssembler = formAssembler;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PartTimeEmployeeCreateVO create(EmployeeOnboardingSaveRequest request, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("登记表内容不能为空");
        }
        String fullName = trimToNull(request.getFullName());
        String phone = trimToNull(request.getPhone());
        if (fullName == null) {
            throw ApiException.badRequest("姓名不能为空");
        }
        if (phone == null) {
            throw ApiException.badRequest("手机号不能为空");
        }
        if (phone.length() > 20) {
            throw ApiException.badRequest("手机号长度不能超过20");
        }
        EmployeePo pending = new EmployeePo();
        applyRequest(pending, request);
        pending.setFullName(fullName);
        pending.setPhone(phone);
        formAssembler.validateRequiredSteps(pending, request, Set.of(OnboardingStep.PHOTO));

        UserPo existingUser = userService.getByUserName(phone);
        if (existingUser != null) {
            throw ApiException.conflict("手机号已存在对应用户账号");
        }

        String initialPassword = generateInitialPassword();
        UserSaveDTO userRequest = new UserSaveDTO();
        userRequest.setUsername(phone);
        userRequest.setPassword(initialPassword);
        userRequest.setPhone(phone);
        userRequest.setEmail(trimToNull(request.getEmail()));
        Integer userId;
        try {
            userId = userService.dealSave(userRequest);
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("手机号已存在对应用户账号");
        }
        if (userId == null || userId <= 0) {
            throw ApiException.internalServerError("系统用户创建失败");
        }

        LocalDateTime now = LocalDateTime.now();
        String employeeUuid = UUID.randomUUID().toString();
        EmployeePo employee = new EmployeePo();
        applyRequest(employee, request);
        employee.setEmployeeUuid(employeeUuid);
        employee.setEmployeeNo(generateEmployeeNo(employeeUuid));
        employee.setUserId(userId);
        employee.setRecordUuid(null);
        employee.setEmploymentType(EmploymentType.PART_TIME.getCode());
        employee.setFullName(fullName);
        employee.setPhone(phone);
        employee.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        employee.setSubmittedAt(now);
        employee.setHrConfirmedAt(now);
        employee.setHrConfirmedBy(operatorUserId);
        employee.setEmploymentStatus(EmploymentStatus.REGULAR.getCode());
        employee.setWorkLocation(WorkLocation.HEADQUARTERS.getCode());
        employee.setHiredAt(LocalDate.now(clock));
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        try {
            if (employeeDao.insert(employee) != 1) {
                throw ApiException.internalServerError("兼职人员档案保存失败");
            }
            childPersistence.replaceAll(employeeUuid, request, now);
        } catch (DuplicateKeyException exception) {
            throw translateDuplicate(exception);
        }

        EmployeePo saved = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (saved == null) {
            throw ApiException.internalServerError("兼职人员档案保存失败");
        }
        PartTimeEmployeeCreateVO result = new PartTimeEmployeeCreateVO();
        result.setUsername(phone);
        result.setInitialPassword(initialPassword);
        result.setEmployee(formAssembler.toForm(saved));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOnboardingFormVO findByEmployeeUuid(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        return formAssembler.toForm(requirePartTimeEmployee(employeeUuid));
    }

    @Override
    @Transactional
    public EmployeeOnboardingFormVO uploadPhoto(String employeeUuid, MultipartFile file, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        validateEmployeeUuid(employeeUuid);
        EmployeePo employee = employeeDao.selectByEmployeeUuidForUpdate(employeeUuid);
        if (employee == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        if (!EmploymentType.PART_TIME.getCode().equals(employee.getEmploymentType())) {
            throw ApiException.unprocessableEntity("该档案不是兼职人员");
        }
        String storedFilename = employeePhotoStorageService.store(file);
        String previousPhoto = employee.getPhotoPath();
        employee.setPhotoPath(storedFilename);
        employee.setUpdatedAt(LocalDateTime.now());
        try {
            if (employeeDao.updateByEmployeeUuid(employee) != 1) {
                employeePhotoStorageService.deleteQuietly(storedFilename);
                throw ApiException.internalServerError("兼职人员档案保存失败");
            }
        } catch (DuplicateKeyException exception) {
            employeePhotoStorageService.deleteQuietly(storedFilename);
            throw translateDuplicate(exception);
        }
        if (previousPhoto != null && !previousPhoto.equals(storedFilename)) {
            employeePhotoStorageService.deleteQuietly(previousPhoto);
        }
        return reloadForm(employeeUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePhotoFileVO loadPhoto(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requirePartTimeEmployee(employeeUuid);
        return employeePhotoStorageService.load(employee.getPhotoPath());
    }

    private EmployeePo requirePartTimeEmployee(String employeeUuid) {
        validateEmployeeUuid(employeeUuid);
        EmployeePo employee = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (employee == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        if (!EmploymentType.PART_TIME.getCode().equals(employee.getEmploymentType())) {
            throw ApiException.unprocessableEntity("该档案不是兼职人员");
        }
        return employee;
    }

    private EmployeeOnboardingFormVO reloadForm(String employeeUuid) {
        EmployeePo saved = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (saved == null) {
            throw ApiException.internalServerError("兼职人员档案保存失败");
        }
        return formAssembler.toForm(saved);
    }

    private static void applyRequest(EmployeePo employee, EmployeeOnboardingSaveRequest request) {
        employee.setGender(trimToNull(request.getGender()));
        employee.setBirthDate(request.getBirthDate());
        employee.setPosition(trimToNull(request.getPosition()));
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

    private static String generateEmployeeNo(String employeeUuid) {
        return "EMP" + employeeUuid.replace("-", "").substring(0, 12).toUpperCase();
    }

    private static String generateInitialPassword() {
        char[] chars = new char[INITIAL_PASSWORD_LENGTH];
        chars[0] = UPPERCASE.charAt(SECURE_RANDOM.nextInt(UPPERCASE.length()));
        chars[1] = LOWERCASE.charAt(SECURE_RANDOM.nextInt(LOWERCASE.length()));
        chars[2] = DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length()));
        for (int index = 3; index < INITIAL_PASSWORD_LENGTH; index++) {
            chars[index] = PASSWORD_ALPHABET.charAt(SECURE_RANDOM.nextInt(PASSWORD_ALPHABET.length()));
        }
        for (int index = chars.length - 1; index > 0; index--) {
            int swapIndex = SECURE_RANDOM.nextInt(index + 1);
            char current = chars[index];
            chars[index] = chars[swapIndex];
            chars[swapIndex] = current;
        }
        return new String(chars);
    }

    private static ApiException translateDuplicate(DuplicateKeyException exception) {
        String message = String.valueOf(exception.getMessage());
        if (message.contains("uk_employee_id_card")) {
            return ApiException.conflict("身份证号已被使用");
        }
        if (message.contains("uk_employee_user_id")) {
            return ApiException.conflict("该用户已存在员工档案");
        }
        if (message.contains("uk_employee_no")) {
            return ApiException.conflict("工号冲突，请重试");
        }
        return ApiException.conflict("员工档案保存冲突");
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
