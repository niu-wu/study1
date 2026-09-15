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

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
