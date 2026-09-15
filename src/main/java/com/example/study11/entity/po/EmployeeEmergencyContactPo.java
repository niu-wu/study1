package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工紧急联络人。 */
@Data
public class EmployeeEmergencyContactPo {

    private Long id;

    private String employeeUuid;

    private Integer sortNo;

    private String fullName;

    private String relationship;

    private String address;

    private String postalCode;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
