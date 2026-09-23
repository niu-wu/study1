package com.example.study11.entity.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工岗位稼动事件。 */
@Data
public class EmployeeAssignmentRecordPo {

    private String assignmentUuid;

    private Long id;

    private String employeeUuid;

    private String assignmentType;

    private LocalDate eventDate;

    private String companyName;

    private BigDecimal utilizationRate;

    private Integer operatorUserId;

    /** 非数据库字段：JOIN user 表带出的经办人登录名。 */
    private String operatorName;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
