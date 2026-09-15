package com.example.study11.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 入职登记培训经历条目。 */
@Data
public class EmployeeTrainingItemDTO {

    private Integer sortNo;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 100, message = "培训机构长度不能超过100")
    private String institution;

    @Size(max = 200, message = "培训内容长度不能超过200")
    private String courseContent;

    @Size(max = 200, message = "培训结果长度不能超过200")
    private String trainingResult;
}
