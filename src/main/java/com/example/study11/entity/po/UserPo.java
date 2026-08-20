package com.example.study11.entity.po;

import lombok.Data;

import java.util.Date;

/**
 * 用户表实体
 */
@Data
public class UserPo {

    private Integer id;

    private String username;

    private String password;

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
