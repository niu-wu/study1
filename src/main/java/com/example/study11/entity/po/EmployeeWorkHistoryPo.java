package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工工作经历。 */
@Data
public class EmployeeWorkHistoryPo {

    private Long id;

    private String employeeUuid;

    private Integer sortNo;

    private LocalDate startDate;

    private LocalDate endDate;

    private String companyName;

    private String position;

    private String leaveReason;

    private String referenceName;

    private String referencePhone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
