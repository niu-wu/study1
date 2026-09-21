package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 员工合同/证件附件列表项。 */
@Data
public class EmployeeContractAttachmentVO {

    private String attachmentUuid;

    private String attachmentType;

    private String attachmentTypeLabel;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
