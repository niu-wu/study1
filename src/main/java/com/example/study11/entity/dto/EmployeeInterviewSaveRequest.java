package com.example.study11.entity.dto;

import com.example.study11.entity.enums.InterviewType;
import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 新增一条面谈记录。姓名/部门/经办人由服务端从档案和 token 推导，不接收前端传入。 */
@Getter
@Setter
public class EmployeeInterviewSaveRequest {

    @NotNull(message = "面谈类型不能为空")
    private InterviewType interviewType;

    @NotNull(message = "面谈时间不能为空")
    private LocalDateTime interviewTime;

    /** 富文本 HTML，入库前经 Jsoup 清洗。 */
    @Size(max = 20000, message = "面谈内容过长")
    private String content;

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
