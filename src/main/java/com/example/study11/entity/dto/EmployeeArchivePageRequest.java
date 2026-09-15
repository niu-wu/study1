package com.example.study11.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 员工档案分页和前缀筛选请求。 */
@Data
public class EmployeeArchivePageRequest {

    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

    private String fullName;

    private String phone;

    private String customerName;

    private String position;

    private String employmentStatus;
}
