package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 发送录用通知请求。 */
@Data
public class OfferNoticeSendRequest {

    @NotBlank(message = "招聘记录 UUID 不能为空")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "招聘记录 UUID 格式不正确")
    private String recordUuid;
}
