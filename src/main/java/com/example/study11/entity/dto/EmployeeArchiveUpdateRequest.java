package com.example.study11.entity.dto;

import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 员工档案有限 PATCH。禁止字段出现在请求体即 400。 */
@Getter
@Setter
public class EmployeeArchiveUpdateRequest {

    private String department;

    private String companyEmail;

    private LocalDate probationEndDate;

    private BigDecimal contractSalary;

    private BigDecimal probationSalary;

    private LocalDate hiredAt;

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
        throw ApiException.badRequest(field + " 不可修改");
    }
}
