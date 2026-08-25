package com.example.study11.controller;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.vo.RecruitmentInfoStatisticsVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentStatusHistoryVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RecruitmentInfoService;
import com.example.study11.service.RecruitmentStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecruitmentInfoControllerTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private RecruitmentInfoService recruitmentInfoService;

    @Mock
    private RecruitmentStatusService recruitmentStatusService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new RecruitmentInfoController(recruitmentInfoService, recruitmentStatusService))
                .build();
    }

    @Test
    void pageReturnsPaginationMetadataAndSerialNumber() throws Exception {
        RecruitmentInfoVO record = new RecruitmentInfoVO();
        record.setSerialNo(3L);
        when(recruitmentInfoService.findPage(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageResult<>(2, 2, 5L, List.of(record)));

        mockMvc.perform(get("/api/recruitment-info/page")
                        .param("page", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.records[0].serialNo").value(3));

        verify(recruitmentInfoService).findPage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void pageRejectsPageSizeAboveConfiguredLimit() throws Exception {
        mockMvc.perform(get("/api/recruitment-info/page")
                        .param("page", "1")
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statisticsReturnsStatusBuckets() throws Exception {
        RecruitmentInfoStatisticsVO statistics = new RecruitmentInfoStatisticsVO();
        statistics.setPendingInitial(1L);
        statistics.setPendingRetest(2L);
        statistics.setPendingOnboarding(3L);
        statistics.setNotPassed(4L);
        when(recruitmentInfoService.findStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/recruitment-info/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingInitial").value(1))
                .andExpect(jsonPath("$.notPassed").value(4));
    }

    @Test
    void statusTransitionUsesAuthenticatedRequestUserAndIgnoresClientOperator() throws Exception {
        RecruitmentInfoVO response = new RecruitmentInfoVO();
        response.setRecordUuid(RECORD_UUID);
        response.setStatus("RETEST_REVIEW");
        when(recruitmentStatusService.transition(eq(RECORD_UUID), any(RecruitmentStatusTransitionDTO.class), eq(15)))
                .thenReturn(response);

        mockMvc.perform(post("/api/recruitment-info/" + RECORD_UUID + "/status")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new Object() {
                            public final String action = StatusTransitionAction.SUBMIT_RETEST_REVIEW.name();
                            public final Integer operatorUserId = 999;
                        })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETEST_REVIEW"));

        verify(recruitmentStatusService).transition(eq(RECORD_UUID), any(RecruitmentStatusTransitionDTO.class), eq(15));
    }

    @Test
    void statusHistoryReturnsHistoryForRecord() throws Exception {
        RecruitmentStatusHistoryVO history = new RecruitmentStatusHistoryVO();
        history.setRecordUuid(RECORD_UUID);
        history.setFromStatus("PENDING_INITIAL");
        history.setToStatus("RETEST_REVIEW");
        history.setOperatorUserId(15);
        when(recruitmentStatusService.findHistory(RECORD_UUID)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/recruitment-info/" + RECORD_UUID + "/status-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStatus").value("PENDING_INITIAL"))
                .andExpect(jsonPath("$[0].operatorUserId").value(15));
    }
}
