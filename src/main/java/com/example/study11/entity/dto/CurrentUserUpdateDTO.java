package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** 当前登录用户资料修改入参，不包含角色、状态和逻辑删除字段。 */
@Data
public class CurrentUserUpdateDTO {

    @NotBlank(message = "用户名不能为空")
    @Length(max = 20, message = "用户名长度不能超过20")
    private String username;

    private String email;

    private String phone;

    private String birthday;
}
