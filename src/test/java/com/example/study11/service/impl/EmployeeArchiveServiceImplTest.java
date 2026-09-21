package com.example.study11.service.impl;

import com.example.study11.common.model.PageResult;
import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeAssignmentRecordDao;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.dao.EmployeeEducationDao;
import com.example.study11.dao.EmployeeEmergencyContactDao;
import com.example.study11.dao.EmployeeContractAttachmentDao;
import com.example.study11.dao.EmployeeContractDao;
import com.example.study11.dao.EmployeeInterviewDao;
import com.example.study11.dao.EmployeeFamilyDao;
import com.example.study11.dao.EmployeeSalaryRecordDao;
import com.example.study11.dao.EmployeeSystemAccountDao;
import com.example.study11.dao.EmployeeTrainingDao;
import com.example.study11.dao.EmployeeWorkHistoryDao;
import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.EmployeeAssignmentSaveRequest;
import com.example.study11.entity.dto.EmployeeArchivePageRequest;
import com.example.study11.entity.dto.EmployeeArchiveUpdateRequest;
import com.example.study11.entity.dto.EmployeeSalarySaveRequest;
import com.example.study11.entity.dto.EmployeeSystemAccountSaveRequest;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.enums.AssignmentType;
import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.enums.WorkLocation;
import com.example.study11.entity.po.EmployeeArchiveStatisticsPo;
import com.example.study11.entity.po.EmployeeAssignmentRecordPo;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.EmployeeSalaryRecordPo;
import com.example.study11.entity.po.EmployeeSystemAccountPo;
import com.example.study11.entity.po.EmployeeTrainingPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.EmployeeArchiveDetailVO;
import com.example.study11.entity.vo.EmployeeArchiveListItemVO;
import com.example.study11.entity.vo.EmployeeArchiveStatisticsVO;
import com.example.study11.entity.vo.EmployeeAssignmentListVO;
import com.example.study11.entity.vo.EmployeeAssignmentRecordVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.EmployeeSalaryListVO;
import com.example.study11.entity.vo.EmployeeSalaryRecordVO;
import com.example.study11.entity.vo.EmployeeSystemAccountVO;
import com.example.study11.exception.ApiException;
import com.example.study11.manager.EmployeeOnboardingProgressCalculator;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.service.RoleAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeArchiveServiceImplTest {

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Mock
    private EmployeeDao employeeDao;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @Mock
    private UserDao userDao;

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
    private EmployeePhotoStorageService employeePhotoStorageService;

    @Mock
    private EmployeeSalaryRecordDao employeeSalaryRecordDao;

    @Mock
    private EmployeeAssignmentRecordDao employeeAssignmentRecordDao;

    @Mock
    private EmployeeSystemAccountDao employeeSystemAccountDao;

    @Mock
    private EmployeeInterviewDao employeeInterviewDao;

    @Mock
    private EmployeeContractDao employeeContractDao;

    @Mock
    private EmployeeContractAttachmentDao employeeContractAttachmentDao;

    private EmployeeArchiveServiceImpl service;

    @BeforeEach
    void setUp() {
        EmployeeFormAssembler assembler = new EmployeeFormAssembler(
                employeeEducationDao, employeeWorkHistoryDao, employeeTrainingDao,
                employeeFamilyDao, employeeEmergencyContactDao,
                new EmployeeOnboardingProgressCalculator());
        Clock clock = Clock.fixed(Instant.parse("2026-09-15T00:00:00+08:00"), ZoneId.of("Asia/Shanghai"));
        service = new EmployeeArchiveServiceImpl(
                employeeDao, roleAuthorizationService, userDao, assembler,
                employeePhotoStorageService, employeeSalaryRecordDao,
                employeeAssignmentRecordDao, employeeSystemAccountDao, employeeInterviewDao, employeeContractDao, employeeContractAttachmentDao, clock);
    }

    @Test
    void pageRequiresHrAndReturnsConfirmedArchivesIncludingNullDates() {
        EmployeeArchivePageRequest request = new EmployeeArchivePageRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setFullName(" 张");
        request.setPhone("139");
        request.setCustomerName("北海");
        request.setPosition("开发");
        request.setEmploymentStatus("PROBATION");

        EmployeePo confirmed = confirmedEmployee();
        confirmed.setHiredAt(null);
        confirmed.setContractSalary(null);
        confirmed.setProbationSalary(null);
        when(employeeDao.countConfirmedArchives(any(EmployeeArchivePageRequest.class))).thenReturn(1L);
        when(employeeDao.selectConfirmedArchives(any(EmployeeArchivePageRequest.class), eq(0L), eq(20)))
                .thenReturn(List.of(confirmed));

        PageResult<EmployeeArchiveListItemVO> result = service.findPage(request, 7);

        assertEquals(1L, result.getTotal());
        assertEquals(EMPLOYEE_UUID, result.getRecords().get(0).getEmployeeUuid());
        assertNull(result.getRecords().get(0).getHiredAt());
        assertNull(result.getRecords().get(0).getContractSalary());
        ArgumentCaptor<EmployeeArchivePageRequest> captor =
                ArgumentCaptor.forClass(EmployeeArchivePageRequest.class);
        verify(employeeDao).selectConfirmedArchives(captor.capture(), eq(0L), eq(20));
        assertEquals("张", captor.getValue().getFullName());
        assertEquals("139", captor.getValue().getPhone());
        assertEquals("北海", captor.getValue().getCustomerName());
        assertEquals("开发", captor.getValue().getPosition());
        assertEquals("PROBATION", captor.getValue().getEmploymentStatus());
        verify(roleAuthorizationService).requireHrOrAdmin(7);
    }

    @Test
    void pageRejectsUserRole() {
        doThrow(ApiException.forbidden("无权执行此操作"))
                .when(roleAuthorizationService).requireHrOrAdmin(51);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.findPage(new EmployeeArchivePageRequest(), 51));

        assertEquals(403, exception.getStatus().value());
        verify(employeeDao, never()).selectConfirmedArchives(any(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void pageRejectsInvalidEmploymentStatus() {
        EmployeeArchivePageRequest request = new EmployeeArchivePageRequest();
        request.setEmploymentStatus("UNKNOWN");

        ApiException exception = assertThrows(ApiException.class, () -> service.findPage(request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeDao, never()).countConfirmedArchives(any());
    }

    @Test
    void statisticsMapsFiveCardsWithoutFilters() {
        EmployeeArchiveStatisticsPo source = new EmployeeArchiveStatisticsPo();
        source.setProbationCount(1L);
        source.setActiveCount(4L);
        source.setHeadquartersCount(3L);
        source.setDispatchedCount(1L);
        source.setResignedCount(1L);
        when(employeeDao.selectArchiveStatistics()).thenReturn(source);

        EmployeeArchiveStatisticsVO result = service.findStatistics(7);

        assertEquals(1L, result.getProbationCount());
        assertEquals(4L, result.getActiveCount());
        assertEquals(3L, result.getHeadquartersCount());
        assertEquals(1L, result.getDispatchedCount());
        assertEquals(1L, result.getResignedCount());
        verify(employeeDao).selectArchiveStatistics();
    }

    @Test
    void updateWritesAllowedFieldsOnly() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        when(employeeDao.updateArchiveFields(any(EmployeePo.class))).thenReturn(1);
        EmployeePo updated = confirmedEmployee();
        updated.setDepartment("研发部");
        updated.setCompanyEmail("hr@example.com");
        updated.setContractSalary(new BigDecimal("9000.00"));
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(updated);

        EmployeeArchiveUpdateRequest request = new EmployeeArchiveUpdateRequest();
        request.setDepartment("研发部");
        request.setCompanyEmail("hr@example.com");
        request.setContractSalary(new BigDecimal("9000.00"));

        EmployeeArchiveListItemVO result = service.update(EMPLOYEE_UUID, request, 7);

        ArgumentCaptor<EmployeePo> captor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).updateArchiveFields(captor.capture());
        assertEquals("研发部", captor.getValue().getDepartment());
        assertEquals("hr@example.com", captor.getValue().getCompanyEmail());
        assertEquals(new BigDecimal("9000.00"), captor.getValue().getContractSalary());
        assertEquals(EmploymentStatus.PROBATION.getCode(), captor.getValue().getEmploymentStatus());
        assertEquals("张三", result.getFullName());
        assertEquals(new BigDecimal("9000.00"), result.getContractSalary());
    }

    @Test
    void updateRejectsForbiddenFields() {
        EmployeeArchiveUpdateRequest request = new EmployeeArchiveUpdateRequest();
        request.captureUnexpectedField("employmentStatus", "REGULAR");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        assertEquals("employmentStatus 不可修改", exception.getMessage());
        verify(employeeDao, never()).updateArchiveFields(any());
    }

    @Test
    void updateRejectsPasswordField() {
        EmployeeArchiveUpdateRequest request = new EmployeeArchiveUpdateRequest();
        request.captureUnexpectedField("password", "secret");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        assertEquals("password 不可修改", exception.getMessage());
    }

    @Test
    void updateRejectsWorkLocationAndCustomerName() {
        EmployeeArchiveUpdateRequest locationRequest = new EmployeeArchiveUpdateRequest();
        locationRequest.captureUnexpectedField("workLocation", "DISPATCHED");
        ApiException locationException = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, locationRequest, 7));
        assertEquals("workLocation 不可修改", locationException.getMessage());

        EmployeeArchiveUpdateRequest customerRequest = new EmployeeArchiveUpdateRequest();
        customerRequest.captureUnexpectedField("customerName", "客户A");
        ApiException customerException = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, customerRequest, 7));
        assertEquals("customerName 不可修改", customerException.getMessage());
    }

    @Test
    void updateRejectsHiredAtWhenAlreadyPresent() {
        EmployeePo employee = confirmedEmployee();
        employee.setHiredAt(LocalDate.of(2026, 9, 1));
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(employee);

        EmployeeArchiveUpdateRequest request = new EmployeeArchiveUpdateRequest();
        request.setHiredAt(LocalDate.of(2026, 9, 10));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, request, 7));

        assertEquals(409, exception.getStatus().value());
        verify(employeeDao, never()).updateArchiveFields(any());
    }

    @Test
    void updateAllowsHiredAtWhenMissing() {
        EmployeePo employee = confirmedEmployee();
        employee.setHiredAt(null);
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(employee);
        when(employeeDao.updateArchiveFields(any(EmployeePo.class))).thenReturn(1);
        EmployeePo updated = confirmedEmployee();
        updated.setHiredAt(LocalDate.of(2026, 9, 10));
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(updated);

        EmployeeArchiveUpdateRequest request = new EmployeeArchiveUpdateRequest();
        request.setHiredAt(LocalDate.of(2026, 9, 10));

        EmployeeArchiveListItemVO result = service.update(EMPLOYEE_UUID, request, 7);

        assertEquals(LocalDate.of(2026, 9, 10), result.getHiredAt());
        verify(employeeDao).updateArchiveFields(any(EmployeePo.class));
    }

    @Test
    void updateReturnsNotFoundForUnconfirmedArchive() {
        EmployeePo pending = confirmedEmployee();
        pending.setHrConfirmedAt(null);
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(pending);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, new EmployeeArchiveUpdateRequest(), 7));

        assertEquals(404, exception.getStatus().value());
        verify(employeeDao, never()).updateArchiveFields(any());
    }

    @Test
    void updateRejectsNegativeSalary() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeArchiveUpdateRequest request = new EmployeeArchiveUpdateRequest();
        request.setContractSalary(new BigDecimal("-1"));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeDao, never()).updateArchiveFields(any());
    }

    @Test
    void pageMapsPartTimeAsRegular() {
        EmployeePo partTime = confirmedEmployee();
        partTime.setEmploymentType(EmploymentType.PART_TIME.getCode());
        partTime.setEmploymentStatus(EmploymentStatus.REGULAR.getCode());
        partTime.setWorkLocation(WorkLocation.HEADQUARTERS.getCode());
        when(employeeDao.countConfirmedArchives(any())).thenReturn(1L);
        when(employeeDao.selectConfirmedArchives(any(), eq(0L), eq(20))).thenReturn(List.of(partTime));

        PageResult<EmployeeArchiveListItemVO> result = service.findPage(new EmployeeArchivePageRequest(), 7);

        assertEquals(EmploymentType.PART_TIME, result.getRecords().get(0).getEmploymentType());
        assertEquals(EmploymentStatus.REGULAR, result.getRecords().get(0).getEmploymentStatus());
    }

    @Test
    void getDetailReturnsHeaderAndTrainingResultWithoutProgress() {
        EmployeePo employee = confirmedEmployee();
        employee.setUserId(88);
        employee.setBirthDate(LocalDate.of(1990, 1, 15));
        employee.setHiredAt(LocalDate.of(2025, 9, 15));
        employee.setProbationEndDate(LocalDate.of(2026, 9, 20));
        employee.setCompanyEmail("zhangsan@company.com");
        employee.setDepartment("研发部");
        employee.setPhotoPath("secret.jpg");
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(employee);
        UserPo user = new UserPo();
        user.setUsername("zhangsan");
        when(userDao.selectUserById(88)).thenReturn(user);
        EmployeeTrainingPo training = new EmployeeTrainingPo();
        training.setCourseContent("Java");
        training.setTrainingResult("合格");
        when(employeeTrainingDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of(training));

        EmployeeArchiveDetailVO detail = service.getDetail(EMPLOYEE_UUID, 7);

        assertEquals("张三", detail.getHeader().getFullName());
        assertEquals(36, detail.getHeader().getAge());
        assertEquals(12, detail.getHeader().getTenureMonths());
        assertEquals(5, detail.getHeader().getProbationRemainingDays());
        assertEquals("zhangsan", detail.getHeader().getHrSystemAccount());
        assertTrue(detail.isPhotoUploaded());
        assertEquals("合格", detail.getTrainings().get(0).getTrainingResult());
        assertNull(detail.getPhotoPath());
    }

    @Test
    void getDetailReturnsNullProbationCountdownForRegularAndZeroWhenExpired() {
        EmployeePo regular = confirmedEmployee();
        regular.setEmploymentStatus(EmploymentStatus.REGULAR.getCode());
        regular.setProbationEndDate(LocalDate.of(2026, 10, 1));
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(regular);

        assertNull(service.getDetail(EMPLOYEE_UUID, 7).getHeader().getProbationRemainingDays());

        EmployeePo expired = confirmedEmployee();
        expired.setProbationEndDate(LocalDate.of(2026, 9, 1));
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(expired);

        assertEquals(0, service.getDetail(EMPLOYEE_UUID, 7).getHeader().getProbationRemainingDays());
    }

    @Test
    void getDetailReturnsNotFoundForUnconfirmedArchive() {
        EmployeePo pending = confirmedEmployee();
        pending.setHrConfirmedAt(null);
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(pending);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.getDetail(EMPLOYEE_UUID, 7));

        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void loadPhotoUsesStoredFilenameAndRejectsUnconfirmedArchive() {
        EmployeePo employee = confirmedEmployee();
        employee.setPhotoPath("stored-uuid.jpg");
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(employee);
        EmployeePhotoFileVO photo = new EmployeePhotoFileVO();
        photo.setFilename("photo.jpg");
        when(employeePhotoStorageService.load("stored-uuid.jpg")).thenReturn(photo);

        EmployeePhotoFileVO result = service.loadPhoto(EMPLOYEE_UUID, 7);

        assertEquals("photo.jpg", result.getFilename());
        verify(employeePhotoStorageService).load("stored-uuid.jpg");

        EmployeePo pending = confirmedEmployee();
        pending.setHrConfirmedAt(null);
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(pending);
        ApiException exception = assertThrows(ApiException.class,
                () -> service.loadPhoto(EMPLOYEE_UUID, 7));
        assertEquals(404, exception.getStatus().value());
        verify(employeePhotoStorageService, never()).load(null);
    }

    @Test
    void saveSalaryComputesGrossAndNetAndNormalizesMonth() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        when(employeeSalaryRecordDao.insert(any())).thenReturn(1);

        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 9, 15));
        request.setBaseSalary(new BigDecimal("5000.00"));
        request.setPositionAllowance(new BigDecimal("800.00"));
        request.setOvertimePay(new BigDecimal("200.00"));
        request.setBonus(new BigDecimal("300.00"));
        request.setSubsidy(new BigDecimal("100.00"));
        request.setOtherPay(new BigDecimal("50.00"));
        request.setSocialInsurance(new BigDecimal("400.00"));
        request.setHousingFund(new BigDecimal("200.00"));
        request.setTaxAmount(new BigDecimal("150.00"));
        request.setScheduledDays(22);
        request.setActualDays(20);
        request.setLeaveDays(2);

        EmployeeSalaryRecordVO result = service.saveSalary(EMPLOYEE_UUID, request, 7);

        ArgumentCaptor<EmployeeSalaryRecordPo> captor = ArgumentCaptor.forClass(EmployeeSalaryRecordPo.class);
        verify(employeeSalaryRecordDao).insert(captor.capture());
        EmployeeSalaryRecordPo saved = captor.getValue();
        assertEquals(LocalDate.of(2026, 9, 1), saved.getSalaryMonth());
        assertEquals(new BigDecimal("6450.00"), saved.getGrossPay());
        assertEquals(new BigDecimal("5700.00"), saved.getNetPay());
        assertEquals(22, saved.getScheduledDays());
        assertEquals(20, saved.getActualDays());
        assertEquals(2, saved.getLeaveDays());
        assertEquals(7, saved.getCreatedBy());
        assertEquals(new BigDecimal("6450.00"), result.getGrossPay());
        assertEquals(new BigDecimal("5700.00"), result.getNetPay());
        assertEquals(LocalDate.of(2026, 9, 1), result.getSalaryMonth());
    }

    @Test
    void saveSalaryRejectsClientGrossPay() {
        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 9, 1));
        request.setBaseSalary(new BigDecimal("5000"));
        request.captureUnexpectedField("grossPay", 5000);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveSalary(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        assertEquals("grossPay 不允许传入", exception.getMessage());
        verify(employeeSalaryRecordDao, never()).insert(any());
    }

    @Test
    void saveSalaryRejectsDuplicateMonth() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        when(employeeSalaryRecordDao.insert(any()))
                .thenThrow(new DuplicateKeyException("uk_employee_salary_month"));

        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 9, 1));
        request.setBaseSalary(new BigDecimal("5000"));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveSalary(EMPLOYEE_UUID, request, 7));

        assertEquals(409, exception.getStatus().value());
    }

    @Test
    void saveSalaryRejectsNegativeAmount() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 9, 1));
        request.setBonus(new BigDecimal("-1"));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveSalary(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeSalaryRecordDao, never()).insert(any());
    }

    @Test
    void saveSalaryRejectsNetPayBelowZero() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 9, 1));
        request.setBaseSalary(new BigDecimal("1000"));
        request.setTaxAmount(new BigDecimal("2000"));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveSalary(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeSalaryRecordDao, never()).insert(any());
    }

    @Test
    void saveSalaryOmitsDayCountsAndDoesNotUseThemInTotals() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        when(employeeSalaryRecordDao.insert(any())).thenReturn(1);

        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 8, 1));
        request.setBaseSalary(new BigDecimal("4000"));

        EmployeeSalaryRecordVO result = service.saveSalary(EMPLOYEE_UUID, request, 7);

        ArgumentCaptor<EmployeeSalaryRecordPo> captor = ArgumentCaptor.forClass(EmployeeSalaryRecordPo.class);
        verify(employeeSalaryRecordDao).insert(captor.capture());
        assertNull(captor.getValue().getScheduledDays());
        assertNull(captor.getValue().getActualDays());
        assertNull(captor.getValue().getLeaveDays());
        assertEquals(new BigDecimal("4000.00"), result.getGrossPay());
        assertEquals(new BigDecimal("4000.00"), result.getNetPay());
    }

    @Test
    void saveSalaryRejectsNegativeDayCount() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeSalarySaveRequest request = new EmployeeSalarySaveRequest();
        request.setSalaryMonth(LocalDate.of(2026, 9, 1));
        request.setLeaveDays(-1);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveSalary(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeSalaryRecordDao, never()).insert(any());
    }

    @Test
    void listSalariesReturnsTotalsInMonthDescWithoutAttendanceFlows() {
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeSalaryRecordPo september = salaryPo("s1", LocalDate.of(2026, 9, 1), "5700.00", "200.00");
        EmployeeSalaryRecordPo august = salaryPo("s2", LocalDate.of(2026, 8, 1), "4000.00", "0.00");
        when(employeeSalaryRecordDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of(september, august));

        EmployeeSalaryListVO result = service.listSalaries(EMPLOYEE_UUID, 7);

        assertEquals(2, result.getRecordCount());
        assertEquals(new BigDecimal("9700.00"), result.getSalaryTotal());
        assertEquals(new BigDecimal("200.00"), result.getOvertimePayTotal());
        assertEquals(LocalDate.of(2026, 9, 1), result.getRecords().get(0).getSalaryMonth());
        assertTrue(result.isDayCountsAreManualSnapshots());
        assertTrue(result.isAttendanceFlowsExcluded());
    }

    private static EmployeeSalaryRecordPo salaryPo(String uuid, LocalDate month, String netPay, String overtime) {
        EmployeeSalaryRecordPo record = new EmployeeSalaryRecordPo();
        record.setSalaryUuid(uuid);
        record.setEmployeeUuid(EMPLOYEE_UUID);
        record.setSalaryMonth(month);
        record.setNetPay(new BigDecimal(netPay));
        record.setOvertimePay(new BigDecimal(overtime));
        return record;
    }

    @Test
    void saveEnterFromHeadquartersWritesCustomerAndDispatched() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        when(employeeAssignmentRecordDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of());
        when(employeeAssignmentRecordDao.insert(any())).thenReturn(1);
        when(employeeDao.updateAssignmentState(any(EmployeePo.class))).thenReturn(1);

        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.ENTER);
        request.setEventDate(LocalDate.of(2026, 9, 10));
        request.setCompanyName("北海客户");
        request.setUtilizationRate(new BigDecimal("80.00"));

        EmployeeAssignmentRecordVO result = service.saveAssignment(EMPLOYEE_UUID, request, 7);

        ArgumentCaptor<EmployeeAssignmentRecordPo> eventCaptor =
                ArgumentCaptor.forClass(EmployeeAssignmentRecordPo.class);
        verify(employeeAssignmentRecordDao).insert(eventCaptor.capture());
        assertEquals(AssignmentType.ENTER.getCode(), eventCaptor.getValue().getAssignmentType());
        assertEquals("北海客户", eventCaptor.getValue().getCompanyName());
        ArgumentCaptor<EmployeePo> employeeCaptor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).updateAssignmentState(employeeCaptor.capture());
        assertEquals(WorkLocation.DISPATCHED.getCode(), employeeCaptor.getValue().getWorkLocation());
        assertEquals("北海客户", employeeCaptor.getValue().getCustomerName());
        assertEquals(AssignmentType.ENTER, result.getAssignmentType());
        assertTrue(result.isInProgress());
        assertEquals("进行中", result.getDurationText());
    }

    @Test
    void saveReturnFromDispatchedClearsCustomer() {
        EmployeePo dispatched = confirmedEmployee();
        dispatched.setWorkLocation(WorkLocation.DISPATCHED.getCode());
        dispatched.setCustomerName("北海客户");
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(dispatched);
        EmployeeAssignmentRecordPo lastEnter = assignmentPo("a1", AssignmentType.ENTER, LocalDate.of(2026, 6, 1),
                "北海客户");
        when(employeeAssignmentRecordDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of(lastEnter));
        when(employeeAssignmentRecordDao.insert(any())).thenReturn(1);
        when(employeeDao.updateAssignmentState(any(EmployeePo.class))).thenReturn(1);

        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.RETURN);
        request.setEventDate(LocalDate.of(2026, 9, 1));

        service.saveAssignment(EMPLOYEE_UUID, request, 7);

        ArgumentCaptor<EmployeePo> employeeCaptor = ArgumentCaptor.forClass(EmployeePo.class);
        verify(employeeDao).updateAssignmentState(employeeCaptor.capture());
        assertEquals(WorkLocation.HEADQUARTERS.getCode(), employeeCaptor.getValue().getWorkLocation());
        assertNull(employeeCaptor.getValue().getCustomerName());
    }

    @Test
    void consecutiveEnterDoesNotWrite() {
        EmployeePo dispatched = confirmedEmployee();
        dispatched.setWorkLocation(WorkLocation.DISPATCHED.getCode());
        dispatched.setCustomerName("北海客户");
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(dispatched);

        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.ENTER);
        request.setEventDate(LocalDate.of(2026, 9, 10));
        request.setCompanyName("另一客户");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveAssignment(EMPLOYEE_UUID, request, 7));

        assertEquals(422, exception.getStatus().value());
        verify(employeeAssignmentRecordDao, never()).insert(any());
        verify(employeeDao, never()).updateAssignmentState(any());
    }

    @Test
    void headquartersReturnDoesNotWrite() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.RETURN);
        request.setEventDate(LocalDate.of(2026, 9, 10));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveAssignment(EMPLOYEE_UUID, request, 7));

        assertEquals(422, exception.getStatus().value());
        verify(employeeAssignmentRecordDao, never()).insert(any());
        verify(employeeDao, never()).updateAssignmentState(any());
    }

    @Test
    void returnWithCompanyNameIsRejected() {
        EmployeePo dispatched = confirmedEmployee();
        dispatched.setWorkLocation(WorkLocation.DISPATCHED.getCode());
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(dispatched);
        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.RETURN);
        request.setEventDate(LocalDate.of(2026, 9, 10));
        request.setCompanyName("北海客户");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveAssignment(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeAssignmentRecordDao, never()).insert(any());
        verify(employeeDao, never()).updateAssignmentState(any());
    }

    @Test
    void eventDateBeforeLastEventDoesNotWrite() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeAssignmentRecordPo last = assignmentPo("a1", AssignmentType.RETURN, LocalDate.of(2026, 9, 10), null);
        when(employeeAssignmentRecordDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of(last));
        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.ENTER);
        request.setEventDate(LocalDate.of(2026, 9, 1));
        request.setCompanyName("北海客户");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveAssignment(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        verify(employeeAssignmentRecordDao, never()).insert(any());
        verify(employeeDao, never()).updateAssignmentState(any());
    }

    @Test
    void assignmentInsertFailureLeavesMasterUnchanged() {
        when(employeeDao.selectByEmployeeUuidForUpdate(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        when(employeeAssignmentRecordDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of());
        when(employeeAssignmentRecordDao.insert(any())).thenThrow(new RuntimeException("insert failed"));

        EmployeeAssignmentSaveRequest request = new EmployeeAssignmentSaveRequest();
        request.setAssignmentType(AssignmentType.ENTER);
        request.setEventDate(LocalDate.of(2026, 9, 10));
        request.setCompanyName("北海客户");

        assertThrows(RuntimeException.class, () -> service.saveAssignment(EMPLOYEE_UUID, request, 7));
        verify(employeeDao, never()).updateAssignmentState(any());
    }

    @Test
    void listAssignmentsDerivesDurationAndCountsInProgressInSummary() {
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(confirmedEmployee());
        EmployeeAssignmentRecordPo enter = assignmentPo("a1", AssignmentType.ENTER, LocalDate.of(2026, 1, 15),
                "北海客户");
        EmployeeAssignmentRecordPo back = assignmentPo("a2", AssignmentType.RETURN, LocalDate.of(2026, 4, 15), null);
        EmployeeAssignmentRecordPo current = assignmentPo("a3", AssignmentType.ENTER, LocalDate.of(2026, 9, 1),
                "新客户");
        when(employeeAssignmentRecordDao.selectByEmployeeUuid(EMPLOYEE_UUID))
                .thenReturn(List.of(enter, back, current));

        EmployeeAssignmentListVO result = service.listAssignments(EMPLOYEE_UUID, 7);

        assertEquals(3, result.getRecords().size());
        assertEquals(LocalDate.of(2026, 4, 15), result.getRecords().get(0).getEndDate());
        assertEquals("3个月", result.getRecords().get(0).getDurationText());
        assertTrue(result.getRecords().get(2).isInProgress());
        assertEquals("进行中", result.getRecords().get(2).getDurationText());
        assertEquals(2, result.getSummary().getProjectCount());
        assertEquals(1, result.getSummary().getInProgressCount());
        assertEquals("3个月14天", result.getSummary().getAccumulatedDurationText());
    }

    @Test
    void saveAccountRejectsPassword() {
        EmployeeSystemAccountSaveRequest request = new EmployeeSystemAccountSaveRequest();
        request.setSystemName("企业邮箱");
        request.setAccountName("a@b.com");
        request.captureUnexpectedField("password", "secret");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.saveAccount(EMPLOYEE_UUID, request, 7));

        assertEquals(400, exception.getStatus().value());
        assertEquals("password 不允许传入", exception.getMessage());
        verify(employeeSystemAccountDao, never()).insert(any());
    }

    @Test
    void listAccountsPrependsReadonlyHrSystemWithoutPassword() {
        EmployeePo employee = confirmedEmployee();
        employee.setUserId(88);
        when(employeeDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(employee);
        UserPo user = new UserPo();
        user.setUsername("zhangsan");
        when(userDao.selectUserById(88)).thenReturn(user);
        EmployeeSystemAccountPo manual = new EmployeeSystemAccountPo();
        manual.setAccountUuid("acc-1");
        manual.setSystemName("企业邮箱");
        manual.setAccountName("zhangsan@company.com");
        when(employeeSystemAccountDao.selectByEmployeeUuid(EMPLOYEE_UUID)).thenReturn(List.of(manual));

        List<EmployeeSystemAccountVO> result = service.listAccounts(EMPLOYEE_UUID, 7);

        assertEquals(2, result.size());
        assertEquals("人力资源系统", result.get(0).getSystemName());
        assertEquals("zhangsan", result.get(0).getAccountName());
        assertTrue(result.get(0).isReadonly());
        assertEquals("企业邮箱", result.get(1).getSystemName());
        assertEquals("acc-1", result.get(1).getAccountUuid());
        for (var field : EmployeeSystemAccountVO.class.getDeclaredFields()) {
            assertTrue(!"password".equalsIgnoreCase(field.getName()));
        }
    }

    private static EmployeeAssignmentRecordPo assignmentPo(String uuid, AssignmentType type, LocalDate eventDate,
                                                           String companyName) {
        EmployeeAssignmentRecordPo record = new EmployeeAssignmentRecordPo();
        record.setAssignmentUuid(uuid);
        record.setEmployeeUuid(EMPLOYEE_UUID);
        record.setAssignmentType(type.getCode());
        record.setEventDate(eventDate);
        record.setCompanyName(companyName);
        return record;
    }

    private static EmployeePo confirmedEmployee() {
        EmployeePo employee = new EmployeePo();
        employee.setEmployeeUuid(EMPLOYEE_UUID);
        employee.setEmployeeNo("EMP7C9E66797425");
        employee.setFullName("张三");
        employee.setGender("男");
        employee.setPhone("13900139000");
        employee.setPosition("开发工程师");
        employee.setEmploymentType(EmploymentType.FULL_TIME.getCode());
        employee.setEmploymentStatus(EmploymentStatus.PROBATION.getCode());
        employee.setWorkLocation(WorkLocation.HEADQUARTERS.getCode());
        employee.setHiredAt(LocalDate.of(2026, 9, 1));
        employee.setHrConfirmedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
        return employee;
    }
}
