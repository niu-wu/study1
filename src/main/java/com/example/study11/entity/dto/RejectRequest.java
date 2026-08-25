package com.example.study11.entity.dto;

import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.TalentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 淘汰候选人请求。 */
@Data
public class RejectRequest {

    @NotBlank(message = "招聘记录 UUID 不能为空")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "招聘记录 UUID 格式不正确")
    private String recordUuid;

    @NotNull(message = "淘汰阶段不能为空")
    private RejectionStage rejectionStage;

    @NotNull(message = "淘汰原因不能为空")
    private RejectionReason rejectionReason;

    @NotNull(message = "人才库分类不能为空")
    private TalentCategory talentCategory;

    @Size(max = 500, message = "处理备注长度不能超过500")
    private String remark;

    @Positive(message = "简历附件编号必须为正数")
    private Long resumeAttachmentId;
}
