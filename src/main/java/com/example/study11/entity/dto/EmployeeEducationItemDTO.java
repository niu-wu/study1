package com.example.study11.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 入职登记学历经历条目。 */
@Data
public class EmployeeEducationItemDTO {

    private Integer sortNo;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 100, message = "院校名称长度不能超过100")
    private String schoolName;

    @Size(max = 100, message = "专业长度不能超过100")
    private String major;

    @Size(max = 50, message = "学历长度不能超过50")
    private String educationLevel;

    @Size(max = 200, message = "证书长度不能超过200")
    private String certificate;
}
