package com.example.study11.controller;

import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RecruitmentInfoService;
import com.example.study11.service.ResumeStorageService;
import com.example.study11.service.RetestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Session 29 参数边界回归测试。 */
@ExtendWith(MockitoExtension.class)
class Session29ValidationTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private RecruitmentInfoService recruitmentInfoService;

    @Mock
    private ResumeStorageService resumeStorageService;

    @Mock
    private RetestService retestService;

    private MockMvc recruitmentMockMvc;

    private MockMvc resumeMockMvc;

    private MockMvc retestMockMvc;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        recruitmentMockMvc = MockMvcBuilders.standaloneSetup(
                        new RecruitmentInfoController(recruitmentInfoService))
                .setControllerAdvice(exceptionHandler)
                .build();
        resumeMockMvc = MockMvcBuilders.standaloneSetup(new ResumeController(resumeStorageService))
                .setControllerAdvice(exceptionHandler)
                .build();
        retestMockMvc = MockMvcBuilders.standaloneSetup(new RetestController(retestService))
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void createRejectsMalformedRecruitmentPhoneBeforeCallingService() throws Exception {
        recruitmentMockMvc.perform(post("/api/recruitment-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicantName\":\"张三\",\"position\":\"Java\","
                                + "\"phone\":\"123456\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recruitmentInfoService);
    }

    @Test
    void retestPathRejectsMalformedRecordUuidBeforeCallingService() throws Exception {
        retestMockMvc.perform(get("/api/retest/{recordUuid}", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(retestService);
    }

    @Test
    void retestBodyRejectsMalformedRecordUuidBeforeCallingService() throws Exception {
        retestMockMvc.perform(post("/api/retest/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordUuid\":\"not-a-uuid\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(retestService);
    }

    @Test
    void resumePathRejectsNonPositiveIdBeforeCallingService() throws Exception {
        resumeMockMvc.perform(get("/api/resumes/{id}", 0))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(resumeStorageService);
    }

    @Test
    void resumeUploadRejectsEmptyFileBeforeCallingService() throws Exception {
        resumeMockMvc.perform(multipart("/api/resumes/upload")
                        .file(new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]))
                        .param("recordUuid", RECORD_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(resumeStorageService);
    }

    @Test
    void resumeUploadRejectsBlankRecordUuidBeforeCallingService() throws Exception {
        resumeMockMvc.perform(multipart("/api/resumes/upload")
                        .file(new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{1}))
                        .param("recordUuid", " ")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 15))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(resumeStorageService);
    }
}
