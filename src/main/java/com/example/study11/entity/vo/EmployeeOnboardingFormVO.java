package com.example.study11.entity.vo;

import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.dto.EmployeeWorkHistoryItemDTO;
import com.example.study11.entity.enums.EmployeeFormStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 入职登记表响应。微信 OpenID 等绑定字段只读。 */
@Data
public class EmployeeOnboardingFormVO {

    private String employeeUuid;

    private Long id;

    private String employeeNo;

    private Integer userId;

    private String recordUuid;

    private EmploymentType employmentType;

    private EmployeeFormStatus formStatus;

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

    /** 仅服务端使用，响应不返回存储文件名。 */
    @JsonIgnore
    private String photoPath;

    private boolean photoUploaded;

    private String wechatAccount;

    private String wechatOpenid;

    private String wechatUnionid;

    private LocalDateTime wechatBoundAt;

    private LocalDateTime submittedAt;

    private LocalDateTime hrConfirmedAt;

    private Integer hrConfirmedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private OnboardingProgressVO progress;

    private List<EmployeeEducationItemDTO> educations = new ArrayList<>();

    private List<EmployeeWorkHistoryItemDTO> workHistories = new ArrayList<>();

    private List<EmployeeTrainingItemDTO> trainings = new ArrayList<>();

    private List<EmployeeFamilyItemDTO> familyMembers = new ArrayList<>();

    private List<EmployeeEmergencyContactItemDTO> emergencyContacts = new ArrayList<>();
}
