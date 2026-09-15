package com.example.study11.entity.dto;

import com.example.study11.entity.enums.AssignmentType;
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

/** 追加一条稼动事件。主档地点/客户只能由该事件回写。 */
@Getter
@Setter
public class EmployeeAssignmentSaveRequest {

    @NotNull(message = "稼动类型不能为空")
    private AssignmentType assignmentType;

    @NotNull(message = "事件日期不能为空")
    private LocalDate eventDate;

    private String companyName;

    private BigDecimal utilizationRate;

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
