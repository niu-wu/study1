package com.example.study11.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工劳动合同列表项。 */
@Data
public class EmployeeContractVO {

    private String contractUuid;

    private String contractNo;

    private String signType;

    private String signTypeLabel;

    private String contractTermType;

    private String contractTermTypeLabel;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal salary;

    private Integer probationMonths;

    private BigDecimal probationSalary;

    private String companyName;

    private String socialSecurityNo;

    private String housingFundNo;

    private String contractStatus;

    private String contractStatusLabel;

    private LocalDateTime createdAt;
}
