package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工培训经历。 */
@Data
public class EmployeeTrainingPo {

    private Long id;

    private String employeeUuid;

    private Integer sortNo;

    private LocalDate startDate;

    private LocalDate endDate;

    private String institution;

    private String courseContent;

    private String trainingResult;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
