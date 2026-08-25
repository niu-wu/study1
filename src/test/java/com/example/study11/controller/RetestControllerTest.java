package com.example.study11.controller;

import com.example.study11.entity.dto.RetestApplicationRequest;
import com.example.study11.entity.dto.RetestReviewRequest;
import com.example.study11.entity.vo.RetestDetailsVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RetestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RetestControllerTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private RetestService retestService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RetestController(retestService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void applyUsesAuthenticatedUserAndReturnsCreatedDetails() throws Exception {
        RetestDetailsVO response = details("RETEST_REVIEW");
        when(retestService.apply(eq(RECORD_UUID), eq("初试通过"), eq(15))).thenReturn(response);

        RetestApplicationRequest request = new RetestApplicationRequest();
        request.setRecordUuid(RECORD_UUID);
        request.setApplicantRemark("初试通过");

        mockMvc.perform(post("/api/retest/apply")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.status").value("RETEST_REVIEW"));

        verify(retestService).apply(eq(RECORD_UUID), eq("初试通过"), eq(15));
    }

    @Test
    void applyDoesNotAcceptClientOperatorAndMapsUnauthenticatedError() throws Exception {
        when(retestService.apply(eq(RECORD_UUID), isNull(), isNull()))
                .thenThrow(ApiException.unauthorized("当前登录用户无效"));

        mockMvc.perform(post("/api/retest/apply")
                        .param("operatorUserId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID + "\"}"))
                .andExpect(status().isUnauthorized());

        verify(retestService).apply(eq(RECORD_UUID), isNull(), isNull());
    }

    @Test
    void applyRejectsBlankRecordUuidBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/retest/apply")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.recordUuid", containsString("招聘记录 UUID")));

        verifyNoInteractions(retestService);
    }

    @Test
    void confirmUsesAuthenticatedReviewerAndReturnsOk() throws Exception {
        RetestDetailsVO response = details("PENDING_RETEST");
        LocalDateTime retestTime = LocalDateTime.of(2026, 8, 30, 10, 0);
        when(retestService.confirm(eq(RECORD_UUID), eq("外派公司"), eq("李经理"), eq(retestTime), eq(26)))
                .thenReturn(response);

        RetestReviewRequest request = new RetestReviewRequest();
        request.setRecordUuid(RECORD_UUID);
        request.setRetestCompany("外派公司");
        request.setRetestContactPerson("李经理");
        request.setRetestTime(retestTime);

        mockMvc.perform(post("/api/retest/confirm")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 26)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"retestCompany\":\"外派公司\",\"retestContactPerson\":\"李经理\","
                                + "\"retestTime\":\"2026-08-30T10:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_RETEST"));

        verify(retestService).confirm(eq(RECORD_UUID), eq("外派公司"), eq("李经理"), eq(retestTime), eq(26));
    }

    @Test
    void confirmMapsIllegalStateToUnprocessableEntity() throws Exception {
        when(retestService.confirm(eq(RECORD_UUID), isNull(), isNull(), isNull(), eq(26)))
                .thenThrow(ApiException.unprocessableEntity("当前招聘状态不允许确认复试"));

        mockMvc.perform(post("/api/retest/confirm")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 26)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID + "\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void findReturnsRetestDetails() throws Exception {
        when(retestService.findByRecordUuid(RECORD_UUID)).thenReturn(details("RETEST_REVIEW"));

        mockMvc.perform(get("/api/retest/{recordUuid}", RECORD_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.status").value("RETEST_REVIEW"));

        verify(retestService).findByRecordUuid(RECORD_UUID);
    }

    private static RetestDetailsVO details(String status) {
        RetestDetailsVO result = new RetestDetailsVO();
        result.setRecordUuid(RECORD_UUID);
        result.setStatus(status);
        return result;
    }
}
