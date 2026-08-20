package com.example.study11.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户出参视图
 * 不含 password 等敏感字段,用于列表和详情响应
 */
@Data
public class UserVO {

    private Integer id;

    private String username;

    private Date createdAt;

    private Date updatetime;

    private String email;

    private String phone;

    private Date birthday;

    /** 状态 1正常 2停用 */
    private Integer status;

    /** 逻辑删除 0正常 1已删除 */
    private Integer isDeleted;
}
