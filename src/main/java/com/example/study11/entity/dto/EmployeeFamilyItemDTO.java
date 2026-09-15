package com.example.study11.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 入职登记家庭成员条目。不含电话。 */
@Data
public class EmployeeFamilyItemDTO {

    private Integer sortNo;

    @Size(max = 50, message = "家庭成员姓名长度不能超过50")
    private String fullName;

    @Size(max = 30, message = "关系长度不能超过30")
    private String relationship;

    @Size(max = 100, message = "工作单位长度不能超过100")
    private String workUnit;

    @Size(max = 100, message = "所任岗位及职务长度不能超过100")
    private String jobTitle;
}
