package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工岗位系统账号，不存密码。 */
@Data
public class EmployeeSystemAccountPo {

    private String accountUuid;

    private Long id;

    private String employeeUuid;

    private String systemName;

    private String accountName;

    private LocalDateTime openedAt;

    private Integer operatorUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
