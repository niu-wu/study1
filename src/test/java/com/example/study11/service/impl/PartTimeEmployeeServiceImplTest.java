package com.example.study11.service.impl;

import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.dao.EmployeeEducationDao;
import com.example.study11.dao.EmployeeEmergencyContactDao;
import com.example.study11.dao.EmployeeFamilyDao;
import com.example.study11.dao.EmployeeTrainingDao;
import com.example.study11.dao.EmployeeWorkHistoryDao;
import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.PartTimeEmployeeCreateVO;
import com.example.study11.exception.ApiException;
import com.example.study11.manager.EmployeeChildPersistence;
import com.example.study11.manager.EmployeeOnboardingProgressCalculator;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.service.RoleAuthorizationService;
import com.example.study11.service.UserService;
import com.example.study11.utils.PasswordPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartTimeEmployeeServiceImplTest {

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
    private UserService userService;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @Mock
    private EmployeePhotoStorageService employeePhotoStorageService;

    private PartTimeEmployeeServiceImpl service;

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @BeforeEach
    void setUp() {
        EmployeeOnboardingProgressCalculator calculator = new EmployeeOnboardingProgressCalculator();
        EmployeeFormAssembler assembler = new EmployeeFormAssembler(
                employeeEducationDao, employeeWorkHistoryDao, employeeTrainingDao,
                employeeFamilyDao, employeeEmergencyContactDao, calculator);
        EmployeeChildPersistence persistence = new EmployeeChildPersistence(
                employeeEducationDao, employeeWorkHistoryDao, employeeTrainingDao,
                employeeFamilyDao, employeeEmergencyContactDao);
        Clock clock = Clock.fixed(Instant.parse("2026-09-15T00:00:00+08:00"), ZoneId.of("Asia/Shanghai"));
        service = new PartTimeEmployeeServiceImpl(
                employeeDao, userService, roleAuthorizationService,
                employeePhotoStorageService, persistence, assembler, clock);
    }

    @Test
    void createPersistsPartTimeEmployeeAndReturnsOneTimePassword() {
        when(userService.getByUserName("13900139000")).thenReturn(null);
        when(userService.dealSave(any())).thenReturn(88);
        when(employeeDao.insert(any(EmployeePo.class))).thenReturn(1);
        when(employeeDao.selectByEmployeeUuid(any())).thenAnswer(invocation -> {
            EmployeePo saved = partTimeEmployee();
            saved.setEmployeeUuid(invocation.getArgument(0));
            return saved;
        });

        PartTimeEmployeeCreateVO result = service.create(completeRequest(), 7);

        ArgumentCaptor<EmployeePo> captor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).insert(captor.capture());
        EmployeePo saved = captor.getValue();
        assertEquals(EmploymentType.PART_TIME.getCode(), saved.getEmploymentType());
        assertEquals(EmployeeFormStatus.SUBMITTED.getCode(), saved.getFormStatus());
        assertEquals(88, saved.getUserId());
        assertNull(saved.getRecordUuid());
        assertEquals(7, saved.getHrConfirmedBy());
        assertEquals("REGULAR", saved.getEmploymentStatus());
        assertEquals("HEADQUARTERS", saved.getWorkLocation());
        assertEquals(LocalDate.of(2026, 9, 15), saved.getHiredAt());
        assertEquals("13900139000", result.getUsername());
        assertTrue(passwordPolicy.isValid(result.getInitialPassword()));
        assertEquals(EmploymentType.PART_TIME, result.getEmployee().getEmploymentType());
    }

    @Test
    void createRejectsExistingUsername() {
        when(userService.getByUserName("13900139000")).thenReturn(new UserPo());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.create(completeRequest(), 7));

        assertEquals(409, exception.getStatus().value());
        verify(employeeDao, never()).insert(any());
    }

    @Test
    void createRejectsIncompleteRequiredStepsWithoutRequiringPhoto() {
        EmployeeOnboardingSaveRequest request = completeRequest();
        request.setFamilyMembers(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> service.create(request, 7));

        assertEquals(400, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("家庭情况"));
        verify(userService, never()).dealSave(any());
    }

    @Test
    void findByEmployeeUuidRejectsFullTimeArchive() {
        EmployeePo fullTime = partTimeEmployee();
        fullTime.setEmploymentType(EmploymentType.FULL_TIME.getCode());
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(fullTime);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.findByEmployeeUuid(EMPLOYEE_UUID, 7));

        assertEquals(422, exception.getStatus().value());
    }

    @Test
    void uploadPhotoUpdatesSubmittedPartTimeRecord() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(partTimeEmployee());
        when(employeePhotoStorageService.store(any())).thenReturn("aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee.jpg");
        when(employeeDao.updateByEmployeeUuid(any(EmployeePo.class))).thenReturn(1);
        EmployeePo updated = partTimeEmployee();
        updated.setPhotoPath("aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee.jpg");
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(updated);

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});
        var result = service.uploadPhoto(EMPLOYEE_UUID, file, 7);

        assertTrue(result.isPhotoUploaded());
        verify(employeeDao).updateByEmployeeUuid(any(EmployeePo.class));
    }

    private static EmployeeOnboardingSaveRequest completeRequest() {
        EmployeeOnboardingSaveRequest request = new EmployeeOnboardingSaveRequest();
        request.setFullName("李兼职");
        request.setGender("女");
        request.setBirthDate(LocalDate.of(1996, 3, 8));
        request.setPhone("13900139000");
        request.setIdCard("110101199603081234");
        request.setCurrentAddress("海城区兼职路");
        request.setWechatAccount("wx_part");
        EmployeeEducationItemDTO education = new EmployeeEducationItemDTO();
        education.setSchoolName("北海大学");
        education.setEducationLevel("本科");
        request.setEducations(List.of(education));
        EmployeeFamilyItemDTO family = new EmployeeFamilyItemDTO();
        family.setFullName("李母");
        family.setRelationship("母亲");
        request.setFamilyMembers(List.of(family));
        EmployeeEmergencyContactItemDTO contact = new EmployeeEmergencyContactItemDTO();
        contact.setFullName("王紧急");
        contact.setPhone("13700001111");
        request.setEmergencyContacts(List.of(contact));
        return request;
    }

    private static EmployeePo partTimeEmployee() {
        EmployeePo employee = new EmployeePo();
        employee.setEmployeeUuid(EMPLOYEE_UUID);
        employee.setEmployeeNo("EMP7C9E66797425");
        employee.setUserId(88);
        employee.setEmploymentType(EmploymentType.PART_TIME.getCode());
        employee.setFullName("李兼职");
        employee.setPhone("13900139000");
        employee.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        return employee;
    }
}
