package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 候选人简历附件持久化对象。 */
@Data
public class CandidateResumePo {

    private Long id;

    private String recordUuid;

    private String originalFilename;

    /** 仅用于服务端受控文件定位，不能直接作为接口响应。 */
    private String storedFilename;

    private Long fileSize;

    private String contentType;

    private Integer uploaderUserId;

    private LocalDateTime uploadedAt;
}
