package com.example.study11.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工月度薪资记录。天数是 HR 手工快照，不参与应发/实发。 */
@Data
public class EmployeeSalaryRecordVO {

    private String salaryUuid;

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

    private LocalDateTime createdAt;
}
