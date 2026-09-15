package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工学历经历。 */
@Data
public class EmployeeEducationPo {

    private Long id;

    private String employeeUuid;

    private Integer sortNo;

    private LocalDate startDate;

    private LocalDate endDate;

    private String schoolName;

    private String major;

    private String educationLevel;

    private String certificate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
