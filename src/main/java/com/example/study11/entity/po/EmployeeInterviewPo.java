package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工面谈交流记录。 */
@Data
public class EmployeeInterviewPo {

    private String interviewUuid;

    private Long id;

    private String employeeUuid;

    private String interviewType;

    private LocalDateTime interviewTime;

    private String content;

    private Integer handlerUserId;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 非数据库字段：JOIN user 表带出的经办人姓名，仅用于列表展示。 */
    private String handlerName;
}
