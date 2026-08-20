package com.example.study11.dao;

import com.example.study11.entity.po.UserPo;
import java.util.List;

/**
 * 用户数据访问层
 */
public interface UserDao {

    // 查询未删除的用户列表
    List<UserPo> findList();

    // 新增用户,回填自增主键
    void insert(UserPo userPo);

    // 按用户名查询(用于唯一性校验)
    UserPo selectByUserName(String username);

    // 按主键查询
    UserPo selectUserById(Integer id);

    // 更新用户基本信息(username/status)
    int updateUserById(UserPo userPo);

    /**
     * 只更新密码和更新时间，避免修改密码流程覆盖用户其他资料
     */
    int updatePasswordById(UserPo userPo);

    // 逻辑删除标记更新
    int updateUserIsDeleted(Integer id, Integer isDeleted);
}
