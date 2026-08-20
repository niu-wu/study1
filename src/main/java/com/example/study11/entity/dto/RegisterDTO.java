package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 注册入参
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Length(message = "用户名长度不能超过20", max = 20)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(message = "密码长度不能超过20", max = 20)
    private String password;

    /** 注册邮箱(可选)，用于密码修改成功通知。 */
    @Email(message = "邮箱格式不正确")
    private String email;
}
