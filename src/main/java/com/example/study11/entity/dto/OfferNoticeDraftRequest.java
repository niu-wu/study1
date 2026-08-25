package com.example.study11.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建录用通知草稿请求。 */
@Data
public class OfferNoticeDraftRequest {

    @NotBlank(message = "招聘记录 UUID 不能为空")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "招聘记录 UUID 格式不正确")
    private String recordUuid;

    @Email(message = "收件邮箱格式不正确")
    @Size(max = 255, message = "收件邮箱长度不能超过255")
    private String recipientEmail;

    @Size(max = 10000, message = "通知内容长度不能超过10000")
    private String noticeContent;
}
