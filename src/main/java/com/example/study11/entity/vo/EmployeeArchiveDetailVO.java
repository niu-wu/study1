package com.example.study11.entity.vo;

import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.dto.EmployeeWorkHistoryItemDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 员工档案个人信息只读详情，不含 progress。 */
@Data
public class EmployeeArchiveDetailVO {

    private EmployeeArchiveHeaderVO header;

    private String employeeUuid;

    private String email;

    private String idCard;

    private LocalDate birthDate;

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

    private String wechatAccount;

    @JsonIgnore
    private String photoPath;

    private boolean photoUploaded;

    private List<EmployeeEducationItemDTO> educations = new ArrayList<>();

    private List<EmployeeWorkHistoryItemDTO> workHistories = new ArrayList<>();

    private List<EmployeeTrainingItemDTO> trainings = new ArrayList<>();

    private List<EmployeeFamilyItemDTO> familyMembers = new ArrayList<>();

    private List<EmployeeEmergencyContactItemDTO> emergencyContacts = new ArrayList<>();
}
