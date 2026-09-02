package com.example.study11.entity.dto;

import com.example.study11.entity.enums.RecruitmentJobStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 岗位分页查询请求。 */
@Data
public class RecruitmentJobPageRequest {

    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

    @Size(max = 100, message = "岗位名称长度不能超过100")
    private String jobName;

    private RecruitmentJobStatus status;
}
