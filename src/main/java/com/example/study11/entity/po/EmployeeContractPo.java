package com.example.study11.entity.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工劳动合同（含续签）。 */
@Data
public class EmployeeContractPo {

    private String contractUuid;

    private Long id;

    private String employeeUuid;

    private String contractNo;

    private String signType;

    private String contractTermType;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal salary;

    private Integer probationMonths;

    private BigDecimal probationSalary;

    private String companyName;

    private String socialSecurityNo;

    private String housingFundNo;

    private String contractStatus;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
