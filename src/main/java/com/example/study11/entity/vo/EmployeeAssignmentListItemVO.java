package com.example.study11.entity.vo;

import com.example.study11.entity.enums.EmployeeFormStatus;
import lombok.Data;

import java.time.LocalDateTime;

/** 人员分配待审核列表项。 */
@Data
public class EmployeeAssignmentListItemVO {

    private String employeeUuid;

    private String employeeNo;

    private String fullName;

    private String phone;

    private String position;

    private EmployeeFormStatus formStatus;

    private LocalDateTime submittedAt;

    private int percent;
}
