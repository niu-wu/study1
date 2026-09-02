package com.example.study11.entity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** 岗位创建请求。状态、删除标识和操作人由后端维护。 */
@Data
public class RecruitmentJobCreateDTO {

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 100, message = "岗位名称长度不能超过100")
    private String jobName;

    @Size(max = 50, message = "岗位编码长度不能超过50")
    private String jobCode;

    private String jobDescription;

    private String recruitmentRequirements;

    @DecimalMin(value = "0.00", message = "最低薪资不能为负数")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.00", message = "最高薪资不能为负数")
    private BigDecimal salaryMax;

    @Size(max = 100, message = "工作地点长度不能超过100")
    private String workLocation;
}
