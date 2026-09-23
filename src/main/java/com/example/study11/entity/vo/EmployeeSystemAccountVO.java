package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工外部系统账号。password 是该系统的口令，不是本系统登录密码。 */
@Data
public class EmployeeSystemAccountVO {

    private String accountUuid;

    private String systemName;

    private String accountName;

    private String password;

    private LocalDateTime openedAt;

    private boolean readonly;
}
