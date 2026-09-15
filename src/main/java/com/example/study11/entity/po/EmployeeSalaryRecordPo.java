package com.example.study11.entity.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工月度薪资快照。天数可空，不参与应发/实发。 */
@Data
public class EmployeeSalaryRecordPo {

    private String salaryUuid;

    private Long id;

    private String employeeUuid;

    private LocalDate salaryMonth;

    private String department;

    private Integer scheduledDays;

    private Integer actualDays;

    private Integer leaveDays;

    private BigDecimal baseSalary;

    private BigDecimal positionAllowance;

    private BigDecimal overtimePay;

    private BigDecimal bonus;

    private BigDecimal subsidy;

    private BigDecimal otherPay;

    private BigDecimal socialInsurance;

    private BigDecimal housingFund;

    private BigDecimal taxAmount;

    private BigDecimal taxRate;

    private BigDecimal grossPay;

    private BigDecimal netPay;

    private String remark;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
