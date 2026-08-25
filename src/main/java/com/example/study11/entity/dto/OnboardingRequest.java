package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 办理入职请求。账号和初始密码由服务端生成。 */
@Data
public class OnboardingRequest {

    @NotBlank(message = "招聘记录 UUID 不能为空")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "招聘记录 UUID 格式不正确")
    private String recordUuid;

    private LocalDate onboardingDate;

    @Size(max = 500, message = "入职备注长度不能超过500")
    private String onboardingNote;
}
