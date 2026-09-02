package com.example.study11.controller;

import com.example.study11.entity.dto.CurrentUserUpdateDTO;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.entity.vo.UserVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrentUserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CurrentUserController(userService)).build();
    }

    @Test
    void returnsCurrentUserFromAuthenticatedRequestAttribute() throws Exception {
        UserVO user = new UserVO();
        user.setId(7);
        user.setUsername("tester");
        user.setRole(UserRole.USER);
        when(userService.getUserDetailsById(7)).thenReturn(user);

        mockMvc.perform(get("/users/me")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void updatesOnlyCurrentUsersProfile() throws Exception {
        UserVO user = new UserVO();
        user.setId(7);
        user.setUsername("tester-new");
        user.setRole(UserRole.USER);
        when(userService.updateCurrentUser(any(Integer.class), any(CurrentUserUpdateDTO.class)))
                .thenReturn(user);

        mockMvc.perform(put("/users/me")
                        .requestAttr(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "tester-new",
                                  "email": "tester@example.com",
                                  "phone": "13800138000",
                                  "birthday": "2000-01-01",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).updateCurrentUser(any(Integer.class), any(CurrentUserUpdateDTO.class));
    }
}
