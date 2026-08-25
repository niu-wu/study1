package com.example.study11.controller;

import com.example.study11.entity.dto.DeclineRequest;
import com.example.study11.entity.dto.RejectRequest;
import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.TalentCategory;
import com.example.study11.entity.vo.RecruitmentRejectionVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RejectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

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
class RejectionControllerTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private RejectionService rejectionService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RejectionController(rejectionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rejectUsesAuthenticatedUserAndReturnsCreatedRecord() throws Exception {
        when(rejectionService.reject(eq(RECORD_UUID), eq(RejectionStage.RETEST),
                eq(RejectionReason.SKILL_MISMATCH), eq(TalentCategory.FUTURE_CONSIDER),
                eq("技能不匹配"), isNull(), eq(15))).thenReturn(rejection("REJECTED"));

        mockMvc.perform(post("/api/rejections/reject")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"rejectionStage\":\"RETEST\","
                                + "\"rejectionReason\":\"SKILL_MISMATCH\","
                                + "\"talentCategory\":\"FUTURE_CONSIDER\","
                                + "\"remark\":\"技能不匹配\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordUuid").value(RECORD_UUID))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(rejectionService).reject(eq(RECORD_UUID), eq(RejectionStage.RETEST),
                eq(RejectionReason.SKILL_MISMATCH), eq(TalentCategory.FUTURE_CONSIDER),
                eq("技能不匹配"), isNull(), eq(15));
    }

    @Test
    void rejectValidatesRequiredTalentCategory() throws Exception {
        mockMvc.perform(post("/api/rejections/reject")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"rejectionStage\":\"RETEST\","
                                + "\"rejectionReason\":\"OTHER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.talentCategory", containsString("人才库分类")));

        verifyNoInteractions(rejectionService);
    }

    @Test
    void declineUsesAuthenticatedUserAndReturnsOk() throws Exception {
        when(rejectionService.decline(eq(RECORD_UUID), eq(TalentCategory.NOT_SUITABLE),
                eq("候选人主动放弃"), eq(15))).thenReturn(rejection("DECLINED"));

        mockMvc.perform(post("/api/rejections/decline")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"talentCategory\":\"NOT_SUITABLE\","
                                + "\"remark\":\"候选人主动放弃\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));

        verify(rejectionService).decline(eq(RECORD_UUID), eq(TalentCategory.NOT_SUITABLE),
                eq("候选人主动放弃"), eq(15));
    }

    @Test
    void rejectMapsTerminalStatusToUnprocessableEntity() throws Exception {
        when(rejectionService.reject(eq(RECORD_UUID), eq(RejectionStage.RETEST),
                eq(RejectionReason.OTHER), eq(TalentCategory.NOT_SUITABLE), isNull(), isNull(), eq(15)))
                .thenThrow(ApiException.unprocessableEntity("终态招聘记录不能淘汰"));

        mockMvc.perform(post("/api/rejections/reject")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"rejectionStage\":\"RETEST\","
                                + "\"rejectionReason\":\"OTHER\","
                                + "\"talentCategory\":\"NOT_SUITABLE\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void talentPoolPassesOptionalFilters() throws Exception {
        when(rejectionService.findTalentPool(TalentCategory.FUTURE_CONSIDER,
                RejectionReason.SKILL_MISMATCH, RejectionStage.RETEST))
                .thenReturn(List.of(rejection("REJECTED")));

        mockMvc.perform(get("/api/rejections/talent-pool")
                        .param("talentCategory", "FUTURE_CONSIDER")
                        .param("rejectionReason", "SKILL_MISMATCH")
                        .param("rejectionStage", "RETEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordUuid").value(RECORD_UUID));

        verify(rejectionService).findTalentPool(TalentCategory.FUTURE_CONSIDER,
                RejectionReason.SKILL_MISMATCH, RejectionStage.RETEST);
    }

    @Test
    void declineDoesNotAcceptClientOperatorAndMapsUnauthenticatedError() throws Exception {
        when(rejectionService.decline(eq(RECORD_UUID), eq(TalentCategory.NOT_SUITABLE), isNull(), isNull()))
                .thenThrow(ApiException.unauthorized("当前登录用户无效"));

        mockMvc.perform(post("/api/rejections/decline")
                        .param("operatorUserId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"" + RECORD_UUID
                                + "\",\"talentCategory\":\"NOT_SUITABLE\"}"))
                .andExpect(status().isUnauthorized());

        verify(rejectionService).decline(eq(RECORD_UUID), eq(TalentCategory.NOT_SUITABLE), isNull(), isNull());
    }

    private static RecruitmentRejectionVO rejection(String status) {
        RecruitmentRejectionVO result = new RecruitmentRejectionVO();
        result.setRecordUuid(RECORD_UUID);
        result.setStatus(status);
        return result;
    }
}
