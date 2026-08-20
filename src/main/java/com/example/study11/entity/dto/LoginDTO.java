package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 登录入参
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Length(message = "用户名长度不能超过20", max = 20)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(message = "密码长度不能超过20", max = 20)
    private String password;
}
