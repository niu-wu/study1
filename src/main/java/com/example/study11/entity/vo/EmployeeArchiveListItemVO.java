package com.example.study11.entity.vo;

import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 员工档案列表项。 */
@Data
public class EmployeeArchiveListItemVO {

    private String employeeUuid;

    private String fullName;

    private String employeeNo;

    private String gender;

    private String phone;

    private EmploymentStatus employmentStatus;

    private LocalDate hiredAt;

    private LocalDate regularizedAt;

    private String position;

    private String customerName;

    private BigDecimal contractSalary;

    private BigDecimal probationSalary;

    private EmploymentType employmentType;
}
