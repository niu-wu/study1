package com.example.study11.entity.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工入职登记主档。 */
@Data
public class EmployeePo {

    private String employeeUuid;

    private Long id;

    private String employeeNo;

    private Integer userId;

    private String recordUuid;

    private String employmentType;

    private String fullName;

    private String gender;

    private LocalDate birthDate;

    private String position;

    private String phone;

    private String email;

    private String idCard;

    private String maritalStatus;

    private String politicalStatus;

    private String nationality;

    private String ethnicity;

    private String nativePlace;

    private String hukouLocation;

    private String currentAddress;

    private String postalCode;

    private String healthStatus;

    private String highestEducation;

    private String major;

    private String professionalTitle;

    private String foreignLanguage;

    private String hobbies;

    private String photoPath;

    private String wechatAccount;

    private String wechatOpenid;

    private String wechatUnionid;

    private LocalDateTime wechatBoundAt;

    private String formStatus;

    private LocalDateTime submittedAt;

    private LocalDateTime hrConfirmedAt;

    private Integer hrConfirmedBy;

    private String department;

    private String companyEmail;

    private String employmentStatus;

    private String workLocation;

    private LocalDate hiredAt;

    private LocalDate probationEndDate;

    private LocalDate regularizedAt;

    private String customerName;

    private BigDecimal contractSalary;

    private BigDecimal probationSalary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
