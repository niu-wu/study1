package com.example.study11.entity.dto;

import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 登记一条系统账号。禁止传入密码。 */
@Getter
@Setter
public class EmployeeSystemAccountSaveRequest {

    @NotBlank(message = "系统名称不能为空")
    private String systemName;

    @NotBlank(message = "账号名不能为空")
    private String accountName;

    private LocalDateTime openedAt;

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
