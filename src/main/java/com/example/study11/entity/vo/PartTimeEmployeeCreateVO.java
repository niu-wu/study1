package com.example.study11.entity.vo;

import lombok.Data;

/** 兼职人员登记成功结果。初始密码只在本次响应出现。 */
@Data
public class PartTimeEmployeeCreateVO {

    private String username;

    private String initialPassword;

    private EmployeeOnboardingFormVO employee;
}
