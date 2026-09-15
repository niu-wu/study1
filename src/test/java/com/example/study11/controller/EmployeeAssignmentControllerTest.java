package com.example.study11.controller;

import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.vo.EmployeeAssignmentConfirmVO;
import com.example.study11.entity.vo.EmployeeAssignmentListItemVO;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.exception.ApiException;
import com.example.study11.exception.GlobalExceptionHandler;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.EmployeeAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeAssignmentControllerTest {

    private static final String EMPLOYEE_UUID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Mock
    private EmployeeAssignmentService employeeAssignmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EmployeeAssignmentController(employeeAssignmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listPendingUsesAuthenticatedUser() throws Exception {
        EmployeeAssignmentListItemVO item = new EmployeeAssignmentListItemVO();
        item.setEmployeeUuid(EMPLOYEE_UUID);
        item.setFullName("张三");
        item.setFormStatus(EmployeeFormStatus.SUBMITTED);
        item.setPercent(60);
        when(employeeAssignmentService.listPending(7)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/employee-assignments")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeUuid").value(EMPLOYEE_UUID))
                .andExpect(jsonPath("$[0].fullName").value("张三"))
                .andExpect(jsonPath("$[0].percent").value(60));

        verify(employeeAssignmentService).listPending(7);
    }

    @Test
    void confirmReturnsSuccessMessage() throws Exception {
        EmployeeAssignmentConfirmVO result = new EmployeeAssignmentConfirmVO();
        result.setEmployeeUuid(EMPLOYEE_UUID);
        result.setFullName("张三");
        result.setMessage("张三 入职成功");
        when(employeeAssignmentService.confirm(EMPLOYEE_UUID, 7)).thenReturn(result);

        mockMvc.perform(post("/api/employee-assignments/" + EMPLOYEE_UUID + "/confirm")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("张三 入职成功"));
    }

    @Test
    void detailHidesPhotoPath() throws Exception {
        EmployeeOnboardingFormVO form = new EmployeeOnboardingFormVO();
        form.setEmployeeUuid(EMPLOYEE_UUID);
        form.setPhotoPath("secret.jpg");
        form.setPhotoUploaded(true);
        when(employeeAssignmentService.getDetail(EMPLOYEE_UUID, 7)).thenReturn(form);

        mockMvc.perform(get("/api/employee-assignments/" + EMPLOYEE_UUID)
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoPath").doesNotExist())
                .andExpect(jsonPath("$.photoUploaded").value(true));
    }

    @Test
    void confirmMapsForbidden() throws Exception {
        when(employeeAssignmentService.confirm(EMPLOYEE_UUID, 51))
                .thenThrow(ApiException.forbidden("无权执行此操作"));

        mockMvc.perform(post("/api/employee-assignments/" + EMPLOYEE_UUID + "/confirm")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 51))
                .andExpect(status().isForbidden());
    }
}
