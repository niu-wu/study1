package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 确认复试安排请求，外派招聘字段由服务层按招聘类型校验。 */
@Data
public class RetestReviewRequest {

    @NotBlank(message = "招聘记录 UUID 不能为空")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "招聘记录 UUID 格式不正确")
    private String recordUuid;

    @Size(max = 255, message = "复试公司长度不能超过255")
    private String retestCompany;

    @Size(max = 50, message = "复试对接人长度不能超过50")
    private String retestContactPerson;

    private LocalDateTime retestTime;
}
