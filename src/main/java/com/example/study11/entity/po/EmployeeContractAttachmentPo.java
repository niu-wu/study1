package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工合同/证件附件（存 OSS 地址）。 */
@Data
public class EmployeeContractAttachmentPo {

    private String attachmentUuid;

    private Long id;

    private String employeeUuid;

    private String attachmentType;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private Integer sortOrder;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
