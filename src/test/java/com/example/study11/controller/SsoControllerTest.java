package com.example.study11.controller;

import com.example.study11.service.SsoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SsoControllerTest {

    private SsoService ssoService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ssoService = mock(SsoService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SsoController(ssoService)).build();
    }

    @Test
    void loginReturnsJsonToken() throws Exception {
        when(ssoService.login(org.mockito.ArgumentMatchers.any())).thenReturn("encrypted-token");

        mockMvc.perform(post("/sso/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hr01",
                                  "password": "Test@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("encrypted-token"));
    }
}
