package com.example.study11.service.impl;

import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.dao.EmployeeEducationDao;
import com.example.study11.dao.EmployeeEmergencyContactDao;
import com.example.study11.dao.EmployeeFamilyDao;
import com.example.study11.dao.EmployeeTrainingDao;
import com.example.study11.dao.EmployeeWorkHistoryDao;
import com.example.study11.dao.OnboardingRecordDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.dto.EmployeeWorkHistoryItemDTO;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.po.EmployeeEducationPo;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.OnboardingRecordPo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.exception.ApiException;
import com.example.study11.manager.EmployeeChildPersistence;
import com.example.study11.manager.EmployeeOnboardingProgressCalculator;
import com.example.study11.service.EmployeePhotoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeOnboardingServiceImplTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    private static final String PHOTO_FILENAME = "550e8400-e29b-41d4-a716-446655440000.jpg";

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
    private OnboardingRecordDao onboardingRecordDao;

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private UserDao userDao;

    @Mock
    private EmployeePhotoStorageService employeePhotoStorageService;

    private EmployeeOnboardingServiceImpl employeeOnboardingService;

    @BeforeEach
    void setUp() {
        EmployeeOnboardingProgressCalculator calculator = new EmployeeOnboardingProgressCalculator();
        EmployeeFormAssembler assembler = new EmployeeFormAssembler(
                employeeEducationDao, employeeWorkHistoryDao, employeeTrainingDao,
                employeeFamilyDao, employeeEmergencyContactDao, calculator);
        EmployeeChildPersistence persistence = new EmployeeChildPersistence(
                employeeEducationDao, employeeWorkHistoryDao, employeeTrainingDao,
                employeeFamilyDao, employeeEmergencyContactDao);
        employeeOnboardingService = new EmployeeOnboardingServiceImpl(
                employeeDao, onboardingRecordDao, recruitmentInfoDao, userDao,
                employeePhotoStorageService, persistence, assembler);
    }

    @Test
    void getMyFormPrefillsDraftFromOnboardedRecruitment() {
        when(employeeDao.selectByUserId(51)).thenReturn(null);
        when(onboardingRecordDao.selectByUserId(51)).thenReturn(onboarding());
        when(recruitmentInfoDao.selectByRecordUuid(RECORD_UUID))
                .thenReturn(recruitment(RecruitmentStatus.ONBOARDED));
        UserPo user = new UserPo();
        user.setId(51);
        user.setBirthday(Date.valueOf(LocalDate.of(1994, 5, 20)));
        when(userDao.selectUserById(51)).thenReturn(user);
        when(employeeDao.insert(any(EmployeePo.class))).thenAnswer(invocation -> {
            EmployeePo employee = invocation.getArgument(0);
            employee.setId(8L);
            return 1;
        });

        EmployeeOnboardingFormVO result = employeeOnboardingService.getMyForm(51);

        ArgumentCaptor<EmployeePo> captor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).insert(captor.capture());
        EmployeePo saved = captor.getValue();
        assertEquals(51, saved.getUserId());
        assertEquals(RECORD_UUID, saved.getRecordUuid());
        assertEquals(EmploymentType.FULL_TIME.getCode(), saved.getEmploymentType());
        assertEquals("张三", saved.getFullName());
        assertEquals("男", saved.getGender());
        assertEquals("Java开发", saved.getPosition());
        assertEquals("13800138000", saved.getPhone());
        assertEquals("zhangsan@example.com", saved.getEmail());
        assertEquals(LocalDate.of(1994, 5, 20), saved.getBirthDate());
        assertEquals(EmployeeFormStatus.DRAFT.getCode(), saved.getFormStatus());
        assertTrue(saved.getEmployeeNo().startsWith("EMP"));
        assertNotNull(saved.getEmployeeUuid());
        assertEquals("张三", result.getFullName());
        assertEquals(EmployeeFormStatus.DRAFT, result.getFormStatus());
        assertEquals(LocalDate.of(1994, 5, 20), result.getBirthDate());
        assertEquals(EmploymentType.FULL_TIME, result.getEmploymentType());
        assertNotNull(result.getProgress());
        assertEquals(10, result.getProgress().getTotalSteps());
    }

    @Test
    void getMyFormReturnsExistingDraftWithoutCreatingAgain() {
        when(employeeDao.selectByUserId(51)).thenReturn(draftEmployee());

        EmployeeOnboardingFormVO result = employeeOnboardingService.getMyForm(51);

        assertEquals(EMPLOYEE_UUID, result.getEmployeeUuid());
        assertEquals("张三", result.getFullName());
        verify(employeeDao, never()).insert(any());
        verifyNoInteractions(onboardingRecordDao, recruitmentInfoDao, userDao);
    }

    @Test
    void getMyFormRejectsUserWithoutOnboardingRecord() {
        when(employeeDao.selectByUserId(51)).thenReturn(null);
        when(onboardingRecordDao.selectByUserId(51)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeOnboardingService.getMyForm(51));

        assertEquals(422, exception.getStatus().value());
        assertEquals("尚未办理入职，无法填写登记表", exception.getMessage());
        verify(employeeDao, never()).insert(any());
    }

    @Test
    void getMyFormRejectsRecruitmentThatIsNotOnboarded() {
        when(employeeDao.selectByUserId(51)).thenReturn(null);
        when(onboardingRecordDao.selectByUserId(51)).thenReturn(onboarding());
        when(recruitmentInfoDao.selectByRecordUuid(RECORD_UUID))
                .thenReturn(recruitment(RecruitmentStatus.PENDING_ONBOARDING));

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeOnboardingService.getMyForm(51));

        assertEquals(422, exception.getStatus().value());
        verify(employeeDao, never()).insert(any());
    }

    @Test
    void getMyFormRejectsUnauthenticatedUser() {
        ApiException exception = assertThrows(ApiException.class,
                () -> employeeOnboardingService.getMyForm(null));

        assertEquals(401, exception.getStatus().value());
        verifyNoInteractions(employeeDao);
    }

    @Test
    void saveDraftReplacesNestedListsAndKeepsDraftStatus() {
        when(employeeDao.selectByUserIdForUpdate(51)).thenReturn(draftEmployee());
        when(employeeDao.updateByEmployeeUuid(any(EmployeePo.class))).thenReturn(1);
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(draftEmployee());
        EmployeeEducationPo educationPo = new EmployeeEducationPo();
        educationPo.setSchoolName("北海大学");
        educationPo.setCertificate("学士学位证");
        educationPo.setSortNo(0);
        when(employeeEducationDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of(educationPo));

        EmployeeOnboardingSaveRequest request = completeRequest();
        EmployeeEducationItemDTO education = new EmployeeEducationItemDTO();
        education.setSchoolName("北海大学");
        education.setEducationLevel("本科");
        education.setCertificate("学士学位证");
        request.setEducations(List.of(education));
        EmployeeFamilyItemDTO family = new EmployeeFamilyItemDTO();
        family.setFullName("张父");
        family.setRelationship("父亲");
        family.setWorkUnit("北海船厂");
        family.setJobTitle("钳工");
        request.setFamilyMembers(List.of(family));
        EmployeeEmergencyContactItemDTO contact = new EmployeeEmergencyContactItemDTO();
        contact.setFullName("李紧急");
        contact.setRelationship("朋友");
        contact.setAddress("海城区");
        contact.setPostalCode("536000");
        contact.setPhone("13900000000");
        request.setEmergencyContacts(List.of(contact));

        EmployeeOnboardingFormVO result = employeeOnboardingService.saveDraft(51, request);

        ArgumentCaptor<EmployeePo> employeeCaptor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).updateByEmployeeUuid(employeeCaptor.capture());
        assertEquals("110101199405201234", employeeCaptor.getValue().getIdCard());
        assertEquals("wx_zhangsan", employeeCaptor.getValue().getWechatAccount());
        assertEquals(EmployeeFormStatus.DRAFT.getCode(), employeeCaptor.getValue().getFormStatus());
        assertNull(employeeCaptor.getValue().getSubmittedAt());
        assertEquals(PHOTO_FILENAME, employeeCaptor.getValue().getPhotoPath());

        ArgumentCaptor<EmployeeEducationPo> educationCaptor =
                ArgumentCaptor.forClass(EmployeeEducationPo.class);
        verify(employeeEducationDao).deleteByEmployeeUuid(EMPLOYEE_UUID);
        verify(employeeEducationDao).insert(educationCaptor.capture());
        assertEquals("北海大学", educationCaptor.getValue().getSchoolName());
        assertEquals("学士学位证", educationCaptor.getValue().getCertificate());

        ArgumentCaptor<com.example.study11.entity.po.EmployeeFamilyPo> familyCaptor =
                ArgumentCaptor.forClass(com.example.study11.entity.po.EmployeeFamilyPo.class);
        verify(employeeFamilyDao).insert(familyCaptor.capture());
        assertEquals("张父", familyCaptor.getValue().getFullName());
        assertEquals("北海船厂", familyCaptor.getValue().getWorkUnit());
        assertEquals(EmployeeFormStatus.DRAFT, result.getFormStatus());
        assertEquals("北海大学", result.getEducations().get(0).getSchoolName());
    }

    @Test
    void saveDraftRejectsSubmittedForm() {
        EmployeePo submitted = draftEmployee();
        submitted.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        submitted.setSubmittedAt(LocalDateTime.of(2026, 9, 11, 10, 0));
        when(employeeDao.selectByUserIdForUpdate(51)).thenReturn(submitted);

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeOnboardingService.saveDraft(51, completeRequest()));

        assertEquals(422, exception.getStatus().value());
        verify(employeeDao, never()).updateByEmployeeUuid(any());
    }

    @Test
    void submitLocksFormAfterSavingLatestContent() {
        when(employeeDao.selectByUserIdForUpdate(51)).thenReturn(draftEmployee());
        when(employeeDao.updateByEmployeeUuid(any(EmployeePo.class))).thenReturn(1);
        EmployeePo submitted = draftEmployee();
        submitted.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        submitted.setIdCard("110101199405201234");
        submitted.setSubmittedAt(LocalDateTime.of(2026, 9, 11, 10, 0));
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(submitted);

        EmployeeOnboardingFormVO result = employeeOnboardingService.submit(51, completeRequest());

        ArgumentCaptor<EmployeePo> captor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).updateByEmployeeUuid(captor.capture());
        assertEquals(EmployeeFormStatus.SUBMITTED.getCode(), captor.getValue().getFormStatus());
        assertNotNull(captor.getValue().getSubmittedAt());
        assertEquals("110101199405201234", captor.getValue().getIdCard());
        assertEquals(EmployeeFormStatus.SUBMITTED, result.getFormStatus());
    }

    @Test
    void submitRequiresCompletedRequiredSteps() {
        when(employeeDao.selectByUserIdForUpdate(51)).thenReturn(draftEmployee());
        EmployeeOnboardingSaveRequest request = completeRequest();
        request.setIdCard(" ");

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeOnboardingService.submit(51, request));

        assertEquals(400, exception.getStatus().value());
        assertTrue(exception.getMessage().contains("请先完成："));
        assertTrue(exception.getMessage().contains("基本信息"));
        verify(employeeDao, never()).updateByEmployeeUuid(any());
    }

    @Test
    void submitRejectsAlreadySubmittedForm() {
        EmployeePo submitted = draftEmployee();
        submitted.setFormStatus(EmployeeFormStatus.SUBMITTED.getCode());
        when(employeeDao.selectByUserIdForUpdate(51)).thenReturn(submitted);

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeOnboardingService.submit(51, completeRequest()));

        assertEquals(422, exception.getStatus().value());
        verify(employeeDao, never()).updateByEmployeeUuid(any());
    }

    @Test
    void uploadPhotoStoresUuidFilenameAndDoesNotAcceptClientPath() {
        when(employeeDao.selectByUserIdForUpdate(51)).thenReturn(draftEmployee());
        when(employeePhotoStorageService.store(any())).thenReturn("aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee.png");
        when(employeeDao.updateByEmployeeUuid(any(EmployeePo.class))).thenReturn(1);
        EmployeePo updated = draftEmployee();
        updated.setPhotoPath("aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee.png");
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(updated);

        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1, 2, 3});
        EmployeeOnboardingFormVO result = employeeOnboardingService.uploadPhoto(51, file);

        ArgumentCaptor<EmployeePo> captor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).updateByEmployeeUuid(captor.capture());
        assertEquals("aaaaaaaa-bbbb-4ccc-8ddd-eeeeeeeeeeee.png", captor.getValue().getPhotoPath());
        verify(employeePhotoStorageService).deleteQuietly(PHOTO_FILENAME);
        assertTrue(result.isPhotoUploaded());
    }

    private static EmployeeOnboardingSaveRequest completeRequest() {
        EmployeeOnboardingSaveRequest request = new EmployeeOnboardingSaveRequest();
        request.setFullName("张三");
        request.setGender("男");
        request.setBirthDate(LocalDate.of(1994, 5, 20));
        request.setPosition("Java开发");
        request.setPhone("13800138000");
        request.setEmail("zhangsan@example.com");
        request.setIdCard("110101199405201234");
        request.setCurrentAddress("海城区某某路1号");
        request.setWechatAccount("wx_zhangsan");
        EmployeeEducationItemDTO education = new EmployeeEducationItemDTO();
        education.setSchoolName("北海大学");
        education.setEducationLevel("本科");
        request.setEducations(List.of(education));
        EmployeeFamilyItemDTO family = new EmployeeFamilyItemDTO();
        family.setFullName("张父");
        family.setRelationship("父亲");
        request.setFamilyMembers(List.of(family));
        EmployeeEmergencyContactItemDTO contact = new EmployeeEmergencyContactItemDTO();
        contact.setFullName("李紧急");
        contact.setPhone("13900000000");
        request.setEmergencyContacts(List.of(contact));
        EmployeeWorkHistoryItemDTO work = new EmployeeWorkHistoryItemDTO();
        work.setCompanyName("北海软件");
        work.setPosition("开发");
        work.setLeaveReason("个人发展");
        work.setReferenceName("王证明");
        work.setReferencePhone("13700000000");
        request.setWorkHistories(List.of(work));
        return request;
    }

    private static EmployeePo draftEmployee() {
        EmployeePo employee = new EmployeePo();
        employee.setEmployeeUuid(EMPLOYEE_UUID);
        employee.setId(8L);
        employee.setEmployeeNo("EMP7C9E66797425");
        employee.setUserId(51);
        employee.setRecordUuid(RECORD_UUID);
        employee.setEmploymentType(EmploymentType.FULL_TIME.getCode());
        employee.setFullName("张三");
        employee.setGender("男");
        employee.setBirthDate(LocalDate.of(1994, 5, 20));
        employee.setPosition("Java开发");
        employee.setPhone("13800138000");
        employee.setEmail("zhangsan@example.com");
        employee.setIdCard("110101199405201234");
        employee.setCurrentAddress("海城区某某路1号");
        employee.setWechatAccount("wx_zhangsan");
        employee.setPhotoPath(PHOTO_FILENAME);
        employee.setFormStatus(EmployeeFormStatus.DRAFT.getCode());
        return employee;
    }

    private static OnboardingRecordPo onboarding() {
        OnboardingRecordPo record = new OnboardingRecordPo();
        record.setId(9L);
        record.setRecordUuid(RECORD_UUID);
        record.setUserId(51);
        record.setOnboardingDate(LocalDate.of(2026, 9, 1));
        return record;
    }

    private static RecruitmentInfoPo recruitment(RecruitmentStatus status) {
        RecruitmentInfoPo recruitment = new RecruitmentInfoPo();
        recruitment.setRecordUuid(RECORD_UUID);
        recruitment.setApplicantName("张三");
        recruitment.setGender("男");
        recruitment.setPosition("Java开发");
        recruitment.setPhone("13800138000");
        recruitment.setEmail("zhangsan@example.com");
        recruitment.setStatus(status.getCode());
        return recruitment;
    }
}
