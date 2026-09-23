package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工外部系统账号。password 是该系统的明文口令，可空。 */
@Data
public class EmployeeSystemAccountPo {

    private String accountUuid;

    private Long id;

    private String employeeUuid;

    private String systemName;

    private String accountName;

    private String password;

    private LocalDateTime openedAt;

    private Integer operatorUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
