package com.example.study11.service;

import com.example.study11.entity.vo.OfferNoticeVO;

/** 录用通知业务接口。 */
public interface OfferNoticeService {

    OfferNoticeVO createDraft(String recordUuid, String recipientEmail, String noticeContent,
                              Integer draftedByUserId);

    OfferNoticeVO send(String recordUuid, Integer senderUserId);

    OfferNoticeVO findByRecordUuid(String recordUuid);
}
