package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 录用通知持久化对象。 */
@Data
public class OfferNoticePo {

    private Long id;

    private String recordUuid;

    private String recipientEmail;

    private String noticeContent;

    private String status;

    private Integer draftedByUserId;

    private LocalDateTime draftedAt;

    private Integer sentByUserId;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
