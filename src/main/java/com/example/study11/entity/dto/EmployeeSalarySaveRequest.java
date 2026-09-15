package com.example.study11.entity.dto;

import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 登记一个月度薪资。应发/实发由服务端计算，禁止客户端传入。 */
@Getter
@Setter
public class EmployeeSalarySaveRequest {

    @NotNull(message = "工资所属月不能为空")
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

    private String remark;

    @JsonIgnore
    private final Map<String, Object> unexpectedFields = new LinkedHashMap<>();

    @JsonAnySetter
    public void captureUnexpectedField(String name, Object value) {
        unexpectedFields.put(name, value);
    }

    public void rejectUnexpectedFields() {
        if (unexpectedFields.isEmpty()) {
            return;
        }
        String field = unexpectedFields.keySet().iterator().next();
        throw ApiException.badRequest(field + " 不允许传入");
    }
}
