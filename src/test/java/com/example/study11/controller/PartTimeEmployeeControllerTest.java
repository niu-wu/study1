package com.example.study11.controller;

import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.PartTimeEmployeeCreateVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.PartTimeEmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PartTimeEmployeeControllerTest {

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Mock
    private PartTimeEmployeeService partTimeEmployeeService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PartTimeEmployeeController(partTimeEmployeeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsInitialPasswordOnce() throws Exception {
        PartTimeEmployeeCreateVO result = new PartTimeEmployeeCreateVO();
        result.setUsername("13900139000");
        result.setInitialPassword("Ab12Cd34");
        EmployeeOnboardingFormVO employee = new EmployeeOnboardingFormVO();
        employee.setEmployeeUuid(EMPLOYEE_UUID);
        employee.setPhotoPath("secret.jpg");
        result.setEmployee(employee);
        when(partTimeEmployeeService.create(any(EmployeeOnboardingSaveRequest.class), eq(7)))
                .thenReturn(result);

        mockMvc.perform(post("/api/part-time-employees")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmployeeOnboardingSaveRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("13900139000"))
                .andExpect(jsonPath("$.initialPassword").value("Ab12Cd34"))
                .andExpect(jsonPath("$.employee.photoPath").doesNotExist());

        verify(partTimeEmployeeService).create(any(EmployeeOnboardingSaveRequest.class), eq(7));
    }

    @Test
    void detailUsesAuthenticatedUser() throws Exception {
        EmployeeOnboardingFormVO form = new EmployeeOnboardingFormVO();
        form.setEmployeeUuid(EMPLOYEE_UUID);
        when(partTimeEmployeeService.findByEmployeeUuid(EMPLOYEE_UUID, 7)).thenReturn(form);

        mockMvc.perform(get("/api/part-time-employees/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeUuid").value(EMPLOYEE_UUID));
    }

    @Test
    void uploadPhotoReturnsCreated() throws Exception {
        EmployeeOnboardingFormVO form = new EmployeeOnboardingFormVO();
        form.setEmployeeUuid(EMPLOYEE_UUID);
        form.setPhotoUploaded(true);
        when(partTimeEmployeeService.uploadPhoto(eq(EMPLOYEE_UUID), any(), eq(7))).thenReturn(form);

        mockMvc.perform(multipart("/api/part-time-employees/" + EMPLOYEE_UUID + "/photo")
                        .file(new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1}))
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photoUploaded").value(true));
    }

    @Test
    void createMapsForbidden() throws Exception {
        when(partTimeEmployeeService.create(any(EmployeeOnboardingSaveRequest.class), eq(51)))
                .thenThrow(ApiException.forbidden("无权执行此操作"));

        mockMvc.perform(post("/api/part-time-employees")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
