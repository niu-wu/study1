package com.example.study11.entity.dto;

import com.example.study11.entity.enums.ContractSignType;
import com.example.study11.entity.enums.ContractStatus;
import com.example.study11.entity.enums.ContractTermType;
import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 新增一份劳动合同。必填只有签订类型，期限和状态有默认值。 */
@Getter
@Setter
public class EmployeeContractSaveRequest {

    @Size(max = 50, message = "合同编号过长")
    private String contractNo;

    @NotNull(message = "签订类型不能为空")
    private ContractSignType signType;

    private ContractTermType contractTermType;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal salary;

    private Integer probationMonths;

    private BigDecimal probationSalary;

    @Size(max = 100, message = "所属公司过长")
    private String companyName;

    @Size(max = 50, message = "社保编号过长")
    private String socialSecurityNo;

    @Size(max = 50, message = "公积金账号过长")
    private String housingFundNo;

    private ContractStatus contractStatus;

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
