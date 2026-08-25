package com.example.study11.controller;

import com.example.study11.entity.dto.OfferNoticeDraftRequest;
import com.example.study11.entity.dto.OfferNoticeSendRequest;
import com.example.study11.entity.enums.NoticeStatus;
import com.example.study11.entity.vo.OfferNoticeVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.OfferNoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
class OfferNoticeControllerTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private OfferNoticeService offerNoticeService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OfferNoticeController(offerNoticeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void draftUsesAuthenticatedUserAndReturnsCreatedNotice() throws Exception {
        when(offerNoticeService.createDraft(eq(RECORD_UUID), eq("candidate@example.com"),
                eq("欢迎加入团队"), eq(15))).thenReturn(notice(NoticeStatus.DRAFT));

        OfferNoticeDraftRequest request = new OfferNoticeDraftRequest();
        request.setRecordUuid(RECORD_UUID);
        request.setRecipientEmail("candidate@example.com");
        request.setNoticeContent("欢迎加入团队");

        mockMvc.perform(post("/api/offers/draft")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(offerNoticeService).createDraft(eq(RECORD_UUID), eq("candidate@example.com"),
                eq("欢迎加入团队"), eq(15));
    }

    @Test
    void draftRejectsBlankRecordUuidBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/offers/draft")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.recordUuid", containsString("招聘记录 UUID")));

        verifyNoInteractions(offerNoticeService);
    }

    @Test
    void sendUsesAuthenticatedUserAndReturnsSentNotice() throws Exception {
        when(offerNoticeService.send(RECORD_UUID, 15)).thenReturn(notice(NoticeStatus.SENT));

        OfferNoticeSendRequest request = new OfferNoticeSendRequest();
        request.setRecordUuid(RECORD_UUID);

        mockMvc.perform(post("/api/offers/send")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(offerNoticeService).send(RECORD_UUID, 15);
    }

    @Test
    void sendMapsNotCompletedRetestToUnprocessableEntity() throws Exception {
        when(offerNoticeService.send(RECORD_UUID, 15))
                .thenThrow(ApiException.unprocessableEntity("复试尚未完成"));

        mockMvc.perform(post("/api/offers/send")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID + "\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void sendDoesNotAcceptClientOperatorAndMapsUnauthenticatedError() throws Exception {
        when(offerNoticeService.send(RECORD_UUID, null))
                .thenThrow(ApiException.unauthorized("当前登录用户无效"));

        mockMvc.perform(post("/api/offers/send")
                        .param("operatorUserId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID + "\"}"))
                .andExpect(status().isUnauthorized());

        verify(offerNoticeService).send(RECORD_UUID, null);
    }

    @Test
    void findReturnsNotice() throws Exception {
        when(offerNoticeService.findByRecordUuid(RECORD_UUID)).thenReturn(notice(NoticeStatus.DRAFT));

        mockMvc.perform(get("/api/offers/{recordUuid}", RECORD_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(offerNoticeService).findByRecordUuid(RECORD_UUID);
    }

    private static OfferNoticeVO notice(NoticeStatus status) {
        OfferNoticeVO result = new OfferNoticeVO();
        result.setRecordUuid(RECORD_UUID);
        result.setStatus(status);
        result.setRecipientEmail("candidate@example.com");
        return result;
    }
}
