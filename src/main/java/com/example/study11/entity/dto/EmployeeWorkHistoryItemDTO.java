package com.example.study11.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** 入职登记工作经历条目。 */
@Data
public class EmployeeWorkHistoryItemDTO {

    private Integer sortNo;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 100, message = "工作单位长度不能超过100")
    private String companyName;

    @Size(max = 100, message = "职位长度不能超过100")
    private String position;

    @Size(max = 200, message = "离职原因长度不能超过200")
    private String leaveReason;

    @Size(max = 50, message = "证明人姓名长度不能超过50")
    private String referenceName;

    @Size(max = 20, message = "证明人电话长度不能超过20")
    private String referencePhone;
}
