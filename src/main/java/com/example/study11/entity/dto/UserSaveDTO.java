package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 创建用户入参
 */
@Data
public class UserSaveDTO {

    @NotBlank(message = "用户名不能为空")
    @Length(message = "用户名长度不能超过20", max = 20)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(message = "密码长度不能超过20", max = 20)
    private String password;

    /** 邮箱(可选) */
    private String email;

    /** 手机号(可选) */
    private String phone;

    /** 生日(可选) */
    private String birthday;
}
