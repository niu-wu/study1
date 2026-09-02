package com.example.study11.service.impl;

import com.example.study11.dao.UserDao;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.entity.po.UserPo;
import com.example.study11.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAuthorizationServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private RoleAuthorizationServiceImpl service;

    @Test
    void rejectsUserRoleForHrOperation() {
        when(userDao.selectUserById(1)).thenReturn(user(1, UserRole.USER));
        ApiException exception = assertThrows(ApiException.class, () -> service.requireHrOrAdmin(1));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void acceptsHrAndAdminRoles() {
        when(userDao.selectUserById(2)).thenReturn(user(2, UserRole.HR));
        assertDoesNotThrow(() -> service.requireHrOrAdmin(2));
        when(userDao.selectUserById(3)).thenReturn(user(3, UserRole.ADMIN));
        assertDoesNotThrow(() -> service.requireHrOrAdmin(3));
    }

    private static UserPo user(int id, UserRole role) {
        UserPo user = new UserPo();
        user.setId(id);
        user.setRole(role);
        user.setStatus(1);
        user.setIsDeleted(0);
        return user;
    }
}
