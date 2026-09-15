package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工家庭成员。 */
@Data
public class EmployeeFamilyPo {

    private Long id;

    private String employeeUuid;

    private Integer sortNo;

    private String fullName;

    private String relationship;

    private String workUnit;

    private String jobTitle;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
