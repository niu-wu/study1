package com.example.study11.controller;

import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.EmployeeOnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeOnboardingControllerTest {

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Mock
    private EmployeeOnboardingService employeeOnboardingService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new EmployeeOnboardingController(employeeOnboardingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyFormUsesAuthenticatedUser() throws Exception {
        when(employeeOnboardingService.getMyForm(51)).thenReturn(form(EmployeeFormStatus.DRAFT));

        mockMvc.perform(get("/api/employee-onboarding/me")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeUuid").value(EMPLOYEE_UUID))
                .andExpect(jsonPath("$.formStatus").value("DRAFT"))
                .andExpect(jsonPath("$.fullName").value("张三"))
                .andExpect(jsonPath("$.photoPath").doesNotExist());

        verify(employeeOnboardingService).getMyForm(51);
    }

    @Test
    void saveDraftUsesAuthenticatedUserAndRejectsInvalidEmail() throws Exception {
        mockMvc.perform(put("/api/employee-onboarding/me")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email", containsString("邮箱")));

        verifyNoInteractions(employeeOnboardingService);
    }

    @Test
    void saveDraftReturnsUpdatedDraft() throws Exception {
        when(employeeOnboardingService.saveDraft(eq(51), any(EmployeeOnboardingSaveRequest.class)))
                .thenReturn(form(EmployeeFormStatus.DRAFT));

        mockMvc.perform(put("/api/employee-onboarding/me")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"张三\",\"idCard\":\"110101199405201234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formStatus").value("DRAFT"));

        verify(employeeOnboardingService).saveDraft(eq(51), any(EmployeeOnboardingSaveRequest.class));
    }

    @Test
    void submitLocksFormForAuthenticatedUser() throws Exception {
        when(employeeOnboardingService.submit(eq(51), any(EmployeeOnboardingSaveRequest.class)))
                .thenReturn(form(EmployeeFormStatus.SUBMITTED));

        mockMvc.perform(post("/api/employee-onboarding/me/submit")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"张三\",\"gender\":\"男\",\"phone\":\"13800138000\","
                                + "\"idCard\":\"110101199405201234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formStatus").value("SUBMITTED"));

        verify(employeeOnboardingService).submit(eq(51), any(EmployeeOnboardingSaveRequest.class));
    }

    @Test
    void getMyFormMapsUnauthenticatedError() throws Exception {
        when(employeeOnboardingService.getMyForm(isNull()))
                .thenThrow(ApiException.unauthorized("当前登录用户无效"));

        mockMvc.perform(get("/api/employee-onboarding/me"))
                .andExpect(status().isUnauthorized());

        verify(employeeOnboardingService).getMyForm(isNull());
    }

    @Test
    void uploadPhotoUsesAuthenticatedUser() throws Exception {
        when(employeeOnboardingService.uploadPhoto(eq(51), any()))
                .thenReturn(form(EmployeeFormStatus.DRAFT));

        mockMvc.perform(multipart("/api/employee-onboarding/me/photo")
                        .file(new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1}))
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photoPath").doesNotExist())
                .andExpect(jsonPath("$.photoUploaded").value(true));

        verify(employeeOnboardingService).uploadPhoto(eq(51), any());
    }

    @Test
    void downloadPhotoStreamsImageWithoutExposingStoragePath() throws Exception {
        EmployeePhotoFileVO photo = new EmployeePhotoFileVO();
        photo.setResource(new ByteArrayResource(new byte[]{9, 8, 7}));
        photo.setContentType("image/jpeg");
        photo.setFilename("photo.jpg");
        when(employeeOnboardingService.loadMyPhoto(51)).thenReturn(photo);

        mockMvc.perform(get("/api/employee-onboarding/me/photo")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Content-Disposition", containsString("photo.jpg")));
    }

    private static EmployeeOnboardingFormVO form(EmployeeFormStatus status) {
        EmployeeOnboardingFormVO result = new EmployeeOnboardingFormVO();
        result.setEmployeeUuid(EMPLOYEE_UUID);
        result.setUserId(51);
        result.setFullName("张三");
        result.setFormStatus(status);
        result.setPhotoPath("secret-stored-name.jpg");
        result.setPhotoUploaded(true);
        return result;
    }
}
