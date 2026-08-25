package com.example.study11.controller;

import com.example.study11.entity.dto.OnboardingRequest;
import com.example.study11.entity.vo.OnboardingRecordVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.OnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
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
class OnboardingControllerTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private OnboardingService onboardingService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OnboardingController(onboardingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void processUsesAuthenticatedOperatorAndReturnsInitialPasswordOnce() throws Exception {
        OnboardingRecordVO response = details();
        response.setInitialPassword("Ab12Cd34");
        when(onboardingService.process(eq(RECORD_UUID), eq(LocalDate.of(2026, 9, 1)),
                eq("正式入职"), eq(15))).thenReturn(response);

        mockMvc.perform(post("/api/onboarding/process")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"onboardingDate\":\"2026-09-01\","
                                + "\"onboardingNote\":\"正式入职\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(51))
                .andExpect(jsonPath("$.initialPassword").value("Ab12Cd34"));

        verify(onboardingService).process(eq(RECORD_UUID), eq(LocalDate.of(2026, 9, 1)),
                eq("正式入职"), eq(15));
    }

    @Test
    void processRejectsBlankRecordUuidBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/onboarding/process")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.recordUuid", containsString("招聘记录 UUID")));

        verifyNoInteractions(onboardingService);
    }

    @Test
    void processDoesNotAcceptClientOperatorAndMapsUnauthenticatedError() throws Exception {
        when(onboardingService.process(eq(RECORD_UUID), isNull(), isNull(), isNull()))
                .thenThrow(ApiException.unauthorized("当前登录用户无效"));

        mockMvc.perform(post("/api/onboarding/process")
                        .param("operatorUserId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID + "\"}"))
                .andExpect(status().isUnauthorized());

        verify(onboardingService).process(eq(RECORD_UUID), isNull(), isNull(), isNull());
    }

    @Test
    void findReturnsOnboardingDetails() throws Exception {
        when(onboardingService.findByRecordUuid(RECORD_UUID)).thenReturn(details());

        mockMvc.perform(get("/api/onboarding/{recordUuid}", RECORD_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.userId").value(51));

        verify(onboardingService).findByRecordUuid(RECORD_UUID);
    }

    private static OnboardingRecordVO details() {
        OnboardingRecordVO result = new OnboardingRecordVO();
        result.setRecordUuid(RECORD_UUID);
        result.setUserId(51);
        result.setUsername("13800138000");
        result.setOnboardingDate(LocalDate.of(2026, 9, 1));
        return result;
    }
}
