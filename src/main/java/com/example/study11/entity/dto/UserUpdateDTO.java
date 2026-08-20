package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新用户入参
 * 不开放 password 修改(密码属敏感字段,普通更新接口不允许改)
 */
@Data
public class UserUpdateDTO {

    private Integer id;

    @NotNull(message = "用户名不能为空")
    @Length(message = "用户名长度不能超过20", max = 20)
    private String username;

    /** 状态 1正常 2停用 */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 邮箱(可选) */
    private String email;

    /** 手机号(可选) */
    private String phone;

    /** 生日(可选) */
    private String birthday;
}
