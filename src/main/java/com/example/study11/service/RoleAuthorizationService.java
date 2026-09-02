package com.example.study11.service;

import com.example.study11.entity.enums.UserRole;

/** 后端角色校验服务。 */
public interface RoleAuthorizationService {

    /** 要求用户具备 HR 或 ADMIN 角色。 */
    void requireHrOrAdmin(Integer userId);

    /** 要求用户具备指定角色之一。 */
    void requireAnyRole(Integer userId, UserRole... roles);
}
