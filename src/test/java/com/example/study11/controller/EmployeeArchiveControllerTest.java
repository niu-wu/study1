package com.example.study11.controller;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.EmployeeArchivePageRequest;
import com.example.study11.entity.dto.EmployeeArchiveUpdateRequest;
import com.example.study11.entity.dto.EmployeeAssignmentSaveRequest;
import com.example.study11.entity.dto.EmployeeSalarySaveRequest;
import com.example.study11.entity.dto.EmployeeSystemAccountSaveRequest;
import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.vo.EmployeeArchiveDetailVO;
import com.example.study11.entity.vo.EmployeeArchiveHeaderVO;
import com.example.study11.entity.vo.EmployeeArchiveListItemVO;
import com.example.study11.entity.vo.EmployeeArchiveStatisticsVO;
import com.example.study11.entity.vo.EmployeeAssignmentListVO;
import com.example.study11.entity.vo.EmployeeAssignmentRecordVO;
import com.example.study11.entity.vo.EmployeeAssignmentSummaryVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.EmployeeSalaryListVO;
import com.example.study11.entity.vo.EmployeeSalaryRecordVO;
import com.example.study11.entity.vo.EmployeeSystemAccountVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.EmployeeArchiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeArchiveControllerTest {

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Mock
    private EmployeeArchiveService employeeArchiveService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EmployeeArchiveController(employeeArchiveService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void pageUsesAuthenticatedUserAndPrefixFilters() throws Exception {
        EmployeeArchiveListItemVO item = listItem();
        when(employeeArchiveService.findPage(any(EmployeeArchivePageRequest.class), eq(7)))
                .thenReturn(new PageResult<>(1, 20, 1L, List.of(item)));

        mockMvc.perform(get("/api/employee-archives/page")
                        .param("fullName", "张")
                        .param("phone", "139")
                        .param("customerName", "北海")
                        .param("position", "开发")
                        .param("employmentStatus", "PROBATION")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].employeeUuid").value(EMPLOYEE_UUID))
                .andExpect(jsonPath("$.records[0].employmentStatus").value("PROBATION"))
                .andExpect(jsonPath("$.records[0].employmentType").value("FULL_TIME"))
                .andExpect(jsonPath("$.records[0].contractSalary").value(8000));

        ArgumentCaptor<EmployeeArchivePageRequest> captor =
                ArgumentCaptor.forClass(EmployeeArchivePageRequest.class);
        verify(employeeArchiveService).findPage(captor.capture(), eq(7));
        assertEquals("张", captor.getValue().getFullName());
        assertEquals("139", captor.getValue().getPhone());
        assertEquals("北海", captor.getValue().getCustomerName());
        assertEquals("开发", captor.getValue().getPosition());
        assertEquals("PROBATION", captor.getValue().getEmploymentStatus());
    }

    @Test
    void pageMapsForbidden() throws Exception {
        when(employeeArchiveService.findPage(any(EmployeeArchivePageRequest.class), eq(51)))
                .thenThrow(ApiException.forbidden("无权执行此操作"));

        mockMvc.perform(get("/api/employee-archives/page")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51))
                .andExpect(status().isForbidden());
    }

    @Test
    void statisticsReturnsFiveCards() throws Exception {
        EmployeeArchiveStatisticsVO statistics = new EmployeeArchiveStatisticsVO();
        statistics.setProbationCount(1L);
        statistics.setActiveCount(3L);
        statistics.setHeadquartersCount(2L);
        statistics.setDispatchedCount(1L);
        statistics.setResignedCount(1L);
        when(employeeArchiveService.findStatistics(7)).thenReturn(statistics);

        mockMvc.perform(get("/api/employee-archives/statistics")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.probationCount").value(1))
                .andExpect(jsonPath("$.activeCount").value(3))
                .andExpect(jsonPath("$.headquartersCount").value(2))
                .andExpect(jsonPath("$.dispatchedCount").value(1))
                .andExpect(jsonPath("$.resignedCount").value(1));
    }

    @Test
    void detailHidesPhotoPathAndProgress() throws Exception {
        EmployeeArchiveDetailVO detail = new EmployeeArchiveDetailVO();
        detail.setEmployeeUuid(EMPLOYEE_UUID);
        detail.setPhotoPath("secret.jpg");
        detail.setPhotoUploaded(true);
        EmployeeArchiveHeaderVO header = new EmployeeArchiveHeaderVO();
        header.setFullName("张三");
        header.setAge(36);
        header.setHrSystemAccount("zhangsan");
        detail.setHeader(header);
        EmployeeTrainingItemDTO training = new EmployeeTrainingItemDTO();
        training.setTrainingResult("合格");
        detail.setTrainings(List.of(training));
        when(employeeArchiveService.getDetail(EMPLOYEE_UUID, 7)).thenReturn(detail);

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeUuid").value(EMPLOYEE_UUID))
                .andExpect(jsonPath("$.photoPath").doesNotExist())
                .andExpect(jsonPath("$.progress").doesNotExist())
                .andExpect(jsonPath("$.photoUploaded").value(true))
                .andExpect(jsonPath("$.header.hrSystemAccount").value("zhangsan"))
                .andExpect(jsonPath("$.trainings[0].trainingResult").value("合格"));
    }

    @Test
    void detailMapsNotFound() throws Exception {
        when(employeeArchiveService.getDetail(EMPLOYEE_UUID, 7))
                .thenThrow(ApiException.notFound("员工档案不存在"));

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadPhotoStreamsImageWithoutExposingStoragePath() throws Exception {
        EmployeePhotoFileVO photo = new EmployeePhotoFileVO();
        photo.setResource(new ByteArrayResource(new byte[]{9, 8, 7}));
        photo.setContentType("image/jpeg");
        photo.setFilename("photo.jpg");
        when(employeeArchiveService.loadPhoto(EMPLOYEE_UUID, 7)).thenReturn(photo);

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID + "/photo")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Content-Disposition", containsString("photo.jpg")));
    }

    @Test
    void downloadPhotoMapsNotFound() throws Exception {
        when(employeeArchiveService.loadPhoto(EMPLOYEE_UUID, 7))
                .thenThrow(ApiException.notFound("员工档案不存在"));

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID + "/photo")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchUpdatesAllowedFields() throws Exception {
        EmployeeArchiveListItemVO item = listItem();
        item.setContractSalary(new BigDecimal("9000.00"));
        when(employeeArchiveService.update(eq(EMPLOYEE_UUID), any(EmployeeArchiveUpdateRequest.class), eq(7)))
                .thenReturn(item);

        mockMvc.perform(patch("/api/employee-archives/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "department", "研发部",
                                "contractSalary", 9000))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeUuid").value(EMPLOYEE_UUID))
                .andExpect(jsonPath("$.contractSalary").value(9000));
    }

    @Test
    void patchRejectsForbiddenEmploymentStatus() throws Exception {
        when(employeeArchiveService.update(eq(EMPLOYEE_UUID), any(EmployeeArchiveUpdateRequest.class), eq(7)))
                .thenThrow(ApiException.badRequest("employmentStatus 不可修改"));

        mockMvc.perform(patch("/api/employee-archives/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employmentStatus\":\"REGULAR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("employmentStatus 不可修改"));
    }

    @Test
    void patchMapsConflictWhenHiredAtAlreadySet() throws Exception {
        when(employeeArchiveService.update(eq(EMPLOYEE_UUID), any(EmployeeArchiveUpdateRequest.class), eq(7)))
                .thenThrow(ApiException.conflict("入职时间已存在，不可修改"));

        mockMvc.perform(patch("/api/employee-archives/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hiredAt\":\"2026-09-01\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createSalaryReturnsCreatedWithServerTotals() throws Exception {
        EmployeeSalaryRecordVO record = new EmployeeSalaryRecordVO();
        record.setSalaryUuid("salary-1");
        record.setSalaryMonth(LocalDate.of(2026, 9, 1));
        record.setGrossPay(new BigDecimal("6450.00"));
        record.setNetPay(new BigDecimal("5700.00"));
        when(employeeArchiveService.saveSalary(eq(EMPLOYEE_UUID), any(EmployeeSalarySaveRequest.class), eq(7)))
                .thenReturn(record);

        mockMvc.perform(post("/api/employee-archives/" + EMPLOYEE_UUID + "/salaries")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "salaryMonth", "2026-09-01",
                                "baseSalary", 5000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.grossPay").value(6450))
                .andExpect(jsonPath("$.netPay").value(5700));
    }

    @Test
    void createSalaryMapsForbiddenGrossPay() throws Exception {
        when(employeeArchiveService.saveSalary(eq(EMPLOYEE_UUID), any(EmployeeSalarySaveRequest.class), eq(7)))
                .thenThrow(ApiException.badRequest("grossPay 不允许传入"));

        mockMvc.perform(post("/api/employee-archives/" + EMPLOYEE_UUID + "/salaries")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"salaryMonth\":\"2026-09-01\",\"grossPay\":5000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("grossPay 不允许传入"));
    }

    @Test
    void listSalariesReturnsTotalsAndSnapshotFlags() throws Exception {
        EmployeeSalaryListVO list = new EmployeeSalaryListVO();
        list.setSalaryTotal(new BigDecimal("9700.00"));
        list.setOvertimePayTotal(new BigDecimal("200.00"));
        list.setRecordCount(2);
        list.setDayCountsAreManualSnapshots(true);
        list.setAttendanceFlowsExcluded(true);
        when(employeeArchiveService.listSalaries(EMPLOYEE_UUID, 7)).thenReturn(list);

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID + "/salaries")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaryTotal").value(9700))
                .andExpect(jsonPath("$.overtimePayTotal").value(200))
                .andExpect(jsonPath("$.recordCount").value(2))
                .andExpect(jsonPath("$.dayCountsAreManualSnapshots").value(true))
                .andExpect(jsonPath("$.attendanceFlowsExcluded").value(true));
    }

    @Test
    void listSalariesMapsNotFound() throws Exception {
        when(employeeArchiveService.listSalaries(EMPLOYEE_UUID, 7))
                .thenThrow(ApiException.notFound("员工档案不存在"));

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID + "/salaries")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAssignmentReturnsCreated() throws Exception {
        EmployeeAssignmentRecordVO record = new EmployeeAssignmentRecordVO();
        record.setAssignmentUuid("asg-1");
        record.setDurationText("进行中");
        record.setInProgress(true);
        when(employeeArchiveService.saveAssignment(eq(EMPLOYEE_UUID), any(EmployeeAssignmentSaveRequest.class), eq(7)))
                .thenReturn(record);

        mockMvc.perform(post("/api/employee-archives/" + EMPLOYEE_UUID + "/assignments")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignmentType\":\"ENTER\",\"eventDate\":\"2026-09-10\",\"companyName\":\"北海客户\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationText").value("进行中"));
    }

    @Test
    void createAssignmentMapsUnprocessable() throws Exception {
        when(employeeArchiveService.saveAssignment(eq(EMPLOYEE_UUID), any(EmployeeAssignmentSaveRequest.class), eq(7)))
                .thenThrow(ApiException.unprocessableEntity("当前状态不允许进入客户"));

        mockMvc.perform(post("/api/employee-archives/" + EMPLOYEE_UUID + "/assignments")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignmentType\":\"ENTER\",\"eventDate\":\"2026-09-10\",\"companyName\":\"北海客户\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listAssignmentsReturnsSummary() throws Exception {
        EmployeeAssignmentListVO list = new EmployeeAssignmentListVO();
        EmployeeAssignmentSummaryVO summary = new EmployeeAssignmentSummaryVO();
        summary.setProjectCount(2);
        summary.setInProgressCount(1);
        summary.setAccumulatedDurationText("3个月14天");
        list.setSummary(summary);
        when(employeeArchiveService.listAssignments(EMPLOYEE_UUID, 7)).thenReturn(list);

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID + "/assignments")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.projectCount").value(2))
                .andExpect(jsonPath("$.summary.inProgressCount").value(1))
                .andExpect(jsonPath("$.summary.accumulatedDurationText").value("3个月14天"));
    }

    @Test
    void createAccountMapsPasswordRejection() throws Exception {
        when(employeeArchiveService.saveAccount(eq(EMPLOYEE_UUID), any(EmployeeSystemAccountSaveRequest.class), eq(7)))
                .thenThrow(ApiException.badRequest("password 不允许传入"));

        mockMvc.perform(post("/api/employee-archives/" + EMPLOYEE_UUID + "/accounts")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"systemName\":\"企业邮箱\",\"accountName\":\"a@b.com\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password 不允许传入"));
    }

    @Test
    void listAccountsHidesPassword() throws Exception {
        EmployeeSystemAccountVO hr = new EmployeeSystemAccountVO();
        hr.setSystemName("人力资源系统");
        hr.setAccountName("zhangsan");
        hr.setReadonly(true);
        when(employeeArchiveService.listAccounts(EMPLOYEE_UUID, 7)).thenReturn(List.of(hr));

        mockMvc.perform(get("/api/employee-archives/" + EMPLOYEE_UUID + "/accounts")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].systemName").value("人力资源系统"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].readonly").value(true));
    }

    private static EmployeeArchiveListItemVO listItem() {
        EmployeeArchiveListItemVO item = new EmployeeArchiveListItemVO();
        item.setEmployeeUuid(EMPLOYEE_UUID);
        item.setFullName("张三");
        item.setEmployeeNo("EMP7C9E66797425");
        item.setGender("男");
        item.setPhone("13900139000");
        item.setEmploymentStatus(EmploymentStatus.PROBATION);
        item.setHiredAt(LocalDate.of(2026, 9, 1));
        item.setPosition("开发工程师");
        item.setContractSalary(new BigDecimal("8000.00"));
        item.setEmploymentType(EmploymentType.FULL_TIME);
        return item;
    }
}
