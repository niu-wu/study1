package com.example.study11.entity.dto;

import com.example.study11.entity.enums.RecruitmentJobStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 公司配额状态动作请求。 */
@Data
public class RecruitmentAllocationStatusTransitionDTO {

    @NotNull(message = "配额目标状态不能为空")
    private RecruitmentJobStatus toStatus;

    @Size(max = 500, message = "状态变更原因长度不能超过500")
    private String remark;
}
