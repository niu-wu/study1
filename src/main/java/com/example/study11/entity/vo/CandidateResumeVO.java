package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 候选人简历附件响应对象，不暴露服务器存储标识或路径。 */
@Data
public class CandidateResumeVO {

    private Long id;

    private String recordUuid;

    private String originalFilename;

    private Long fileSize;

    private String contentType;

    private Integer uploaderUserId;

    private LocalDateTime uploadedAt;
}
