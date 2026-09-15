package com.example.study11.entity.vo;

import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import lombok.Data;

import java.time.LocalDate;

/** 员工档案详情头图。年龄、工龄、转正倒计时不落库。 */
@Data
public class EmployeeArchiveHeaderVO {

    private String employeeUuid;

    private String fullName;

    private String employeeNo;

    private EmploymentStatus employmentStatus;

    private String gender;

    private Integer age;

    private String phone;

    private String companyEmail;

    private String department;

    private String position;

    private Integer tenureMonths;

    private Integer probationRemainingDays;

    private LocalDate hiredAt;

    private String hrSystemAccount;

    private EmploymentType employmentType;
}
