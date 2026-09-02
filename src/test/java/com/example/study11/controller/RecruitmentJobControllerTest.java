package com.example.study11.controller;

import com.example.study11.entity.dto.RecruitmentJobCreateDTO;
import com.example.study11.entity.vo.RecruitmentJobVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RecruitmentJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecruitmentJobControllerTest {

    private RecruitmentJobService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(RecruitmentJobService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RecruitmentJobController(service)).build();
    }

    @Test
    void createsJobUsingAuthenticatedUserIdAndIgnoresClientStatus() throws Exception {
        RecruitmentJobVO response = new RecruitmentJobVO();
        response.setJobUuid("123e4567-e89b-12d3-a456-426614174000");
        response.setStatus("OPEN");
        when(service.create(any(RecruitmentJobCreateDTO.class), eq(7))).thenReturn(response);

        mockMvc.perform(post("/api/recruitment-jobs")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jobName":"Java工程师","jobDescription":"<p>开发</p><script>x</script>","status":"CLOSED","isDeleted":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(service).create(any(RecruitmentJobCreateDTO.class), eq(7));
    }
}
