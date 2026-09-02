package com.example.study11.service.impl;

import com.example.study11.dao.UserDao;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.entity.po.UserPo;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RoleAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/** 后端角色校验实现，身份和角色均从数据库读取。 */
@Service
@RequiredArgsConstructor
public class RoleAuthorizationServiceImpl implements RoleAuthorizationService {

    private final UserDao userDao;

    @Override
    public void requireHrOrAdmin(Integer userId) {
        requireAnyRole(userId, UserRole.HR, UserRole.ADMIN);
    }

    @Override
    public void requireAnyRole(Integer userId, UserRole... roles) {
        if (userId == null || userId <= 0) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        UserPo user = userDao.selectUserById(userId);
        if (user == null || user.getIsDeleted() == null || user.getIsDeleted() != 0
                || user.getStatus() == null || user.getStatus() != 1) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户会话无效");
        }
        UserRole actualRole = user.getRole();
        if (actualRole == null || Arrays.stream(roles).noneMatch(actualRole::equals)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权执行此操作");
        }
    }
}
