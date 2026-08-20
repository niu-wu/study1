package com.example.study11.service;

import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.dto.UserUpdateDTO;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.UserVO;

import java.util.List;

/**
 * 用户业务层。
 */
public interface UserService {

    // 查询用户列表(不含敏感字段)
    List<UserVO> findList();

    // 创建用户,返回新用户 id
    Integer dealSave(UserSaveDTO userSaveDTO);

    // 按用户名查询用户(登录/唯一性校验用)
    UserPo getByUserName(String username);

    // 查询用户详情(不含敏感字段),不存在抛统一异常
    UserVO getUserDetailsById(Integer id);

    // 更新用户基本信息
    void updateUserById(UserUpdateDTO userUpdateDTO);

    // 逻辑删除用户
    void deleteUserById(Integer id);
}
