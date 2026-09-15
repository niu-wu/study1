package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工系统账号。不返回密码。 */
@Data
public class EmployeeSystemAccountVO {

    private String accountUuid;

    private String systemName;

    private String accountName;

    private LocalDateTime openedAt;

    private boolean readonly;
}
