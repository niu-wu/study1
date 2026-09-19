package com.example.study11.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 登录成功响应。Token 放在 JSON 字段里，便于契约校验和客户端解析。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
}
