package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 人员分配审核通过结果，供「姓名 入职成功」弹窗使用。 */
@Data
public class EmployeeAssignmentConfirmVO {

    private String employeeUuid;

    private String fullName;

    private String message;

    private LocalDateTime hrConfirmedAt;
}
