package com.example.study11.entity.vo;

import com.example.study11.entity.enums.NoticeStatus;
import lombok.Data;

import java.time.LocalDateTime;

/** 录用通知响应对象。 */
@Data
public class OfferNoticeVO {

    private Long id;

    private String recordUuid;

    private String recipientEmail;

    private String noticeContent;

    private NoticeStatus status;

    private Integer draftedByUserId;

    private LocalDateTime draftedAt;

    private Integer sentByUserId;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
