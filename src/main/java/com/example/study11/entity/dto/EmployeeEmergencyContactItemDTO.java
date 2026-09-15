package com.example.study11.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 入职登记紧急联络人条目。 */
@Data
public class EmployeeEmergencyContactItemDTO {

    private Integer sortNo;

    @Size(max = 50, message = "紧急联络人姓名长度不能超过50")
    private String fullName;

    @Size(max = 30, message = "关系长度不能超过30")
    private String relationship;

    @Size(max = 200, message = "联系地址长度不能超过200")
    private String address;

    @Size(max = 10, message = "邮编长度不能超过10")
    private String postalCode;

    @Size(max = 20, message = "电话长度不能超过20")
    private String phone;
}
