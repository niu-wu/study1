package com.example.study11.service.impl;

import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.po.UserPo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 用户创建时密码摘要写入回归测试。 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 创建用户时应保存密码摘要而不是明文() {
        UserSaveDTO request = new UserSaveDTO();
        request.setUsername("new-user");
        request.setPassword("Bb234567");
        when(userDao.selectByUserName("new-user")).thenReturn(null);
        when(passwordEncoder.encode("Bb234567")).thenReturn("$2a$encoded");

        userService.dealSave(request);

        ArgumentCaptor<UserPo> captor = ArgumentCaptor.forClass(UserPo.class);
        verify(userDao).insert(captor.capture());
        assertEquals("$2a$encoded", captor.getValue().getPassword());
    }
}
