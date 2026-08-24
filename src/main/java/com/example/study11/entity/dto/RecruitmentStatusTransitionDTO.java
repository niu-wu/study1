package com.example.study11.entity.dto;

import com.example.study11.entity.enums.StatusTransitionAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 招聘状态动作请求，不接受客户端传入操作人或目标状态。 */
@Data
public class RecruitmentStatusTransitionDTO {

    @NotNull(message = "状态动作不能为空")
    private StatusTransitionAction action;

    @Size(max = 500, message = "状态变更备注长度不能超过500")
    private String remark;
}
