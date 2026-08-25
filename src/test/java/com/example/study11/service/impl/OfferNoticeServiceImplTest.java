package com.example.study11.service.impl;

import com.example.study11.dao.OfferNoticeDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.NoticeStatus;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.OfferNoticePo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.vo.OfferNoticeVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferNoticeServiceImplTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private OfferNoticeDao offerNoticeDao;

    @Mock
    private RecruitmentStatusService recruitmentStatusService;

    @InjectMocks
    private OfferNoticeServiceImpl offerNoticeService;

    @Test
    void createDraftStoresRecipientAndContentWithoutChangingRecruitmentStatus() {
        RecruitmentInfoPo recruitment = recruitment("record-1", RecruitmentStatus.PENDING_RETEST);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(recruitment);
        when(offerNoticeDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(offerNoticeDao.insert(any(OfferNoticePo.class))).thenAnswer(invocation -> {
            OfferNoticePo notice = invocation.getArgument(0);
            notice.setId(7L);
            return 1;
        });
        OfferNoticePo saved = notice(7L, "record-1", "candidate@example.com", NoticeStatus.DRAFT);
        when(offerNoticeDao.selectByRecordUuid("record-1")).thenReturn(null, saved);

        OfferNoticeVO result = offerNoticeService.createDraft(
                "record-1", "candidate@example.com", "欢迎加入团队", 15);

        ArgumentCaptor<OfferNoticePo> captor = ArgumentCaptor.forClass(OfferNoticePo.class);
        verify(offerNoticeDao).insert(captor.capture());
        assertEquals("record-1", captor.getValue().getRecordUuid());
        assertEquals("candidate@example.com", captor.getValue().getRecipientEmail());
        assertEquals("欢迎加入团队", captor.getValue().getNoticeContent());
        assertEquals(15, captor.getValue().getDraftedByUserId());
        assertEquals(NoticeStatus.DRAFT.getCode(), captor.getValue().getStatus());
        assertEquals(NoticeStatus.DRAFT, result.getStatus());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void createDraftRejectsExistingNotice() {
        when(recruitmentInfoDao.selectByRecordUuid("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_RETEST));
        when(offerNoticeDao.selectByRecordUuid("record-1"))
                .thenReturn(notice(7L, "record-1", "candidate@example.com", NoticeStatus.DRAFT));

        ApiException exception = assertThrows(ApiException.class,
                () -> offerNoticeService.createDraft("record-1", "candidate@example.com", null, 15));

        assertEquals(409, exception.getStatus().value());
        verify(offerNoticeDao, never()).insert(any());
    }

    @Test
    void createDraftMapsUniqueConstraintConflictToConflictResponse() {
        when(recruitmentInfoDao.selectByRecordUuid("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_RETEST));
        when(offerNoticeDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(offerNoticeDao.insert(any(OfferNoticePo.class)))
                .thenThrow(new DuplicateKeyException("uk_offer_notice_record_uuid"));

        ApiException exception = assertThrows(ApiException.class,
                () -> offerNoticeService.createDraft("record-1", "candidate@example.com", null, 15));

        assertEquals(409, exception.getStatus().value());
    }

    @Test
    void sendRejectsMissingRecipientEmailBeforeStatusTransition() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_RETEST));
        when(offerNoticeDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(notice(7L, "record-1", null, NoticeStatus.DRAFT));

        ApiException exception = assertThrows(ApiException.class,
                () -> offerNoticeService.send("record-1", 15));

        assertEquals(400, exception.getStatus().value());
        verifyNoInteractions(recruitmentStatusService);
        verify(offerNoticeDao, never()).updateToSent(any(), any(), any());
    }

    @Test
    void sendRejectsWhenRetestIsNotCompleted() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW));
        when(offerNoticeDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(notice(7L, "record-1", "candidate@example.com", NoticeStatus.DRAFT));

        ApiException exception = assertThrows(ApiException.class,
                () -> offerNoticeService.send("record-1", 15));

        assertEquals(422, exception.getStatus().value());
        verifyNoInteractions(recruitmentStatusService);
        verify(offerNoticeDao, never()).updateToSent(any(), any(), any());
    }

    @Test
    void sendTransitionsCompletedRetestAndMarksNoticeSent() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_RETEST));
        when(offerNoticeDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(notice(7L, "record-1", "candidate@example.com", NoticeStatus.DRAFT));
        RecruitmentInfoVO transitioned = new RecruitmentInfoVO();
        transitioned.setRecordUuid("record-1");
        transitioned.setStatus(RecruitmentStatus.PENDING_ONBOARDING.getCode());
        when(recruitmentStatusService.transition(eq("record-1"), any(RecruitmentStatusTransitionDTO.class),
                eq(15))).thenReturn(transitioned);
        when(offerNoticeDao.updateToSent(eq("record-1"), eq(15), any(LocalDateTime.class))).thenReturn(1);
        OfferNoticePo sent = notice(7L, "record-1", "candidate@example.com", NoticeStatus.SENT);
        sent.setSentByUserId(15);
        when(offerNoticeDao.selectByRecordUuid("record-1")).thenReturn(sent);

        OfferNoticeVO result = offerNoticeService.send("record-1", 15);

        ArgumentCaptor<RecruitmentStatusTransitionDTO> transitionCaptor =
                ArgumentCaptor.forClass(RecruitmentStatusTransitionDTO.class);
        verify(recruitmentStatusService).transition(eq("record-1"), transitionCaptor.capture(), eq(15));
        assertEquals(StatusTransitionAction.COMPLETE_RETEST, transitionCaptor.getValue().getAction());
        verify(offerNoticeDao).updateToSent(eq("record-1"), eq(15), any(LocalDateTime.class));
        assertEquals(NoticeStatus.SENT, result.getStatus());
        assertEquals("candidate@example.com", result.getRecipientEmail());
        assertNotNull(result.getSentAt());
    }

    @Test
    void sendRejectsAlreadySentNotice() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING));
        when(offerNoticeDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(notice(7L, "record-1", "candidate@example.com", NoticeStatus.SENT));

        ApiException exception = assertThrows(ApiException.class,
                () -> offerNoticeService.send("record-1", 15));

        assertEquals(409, exception.getStatus().value());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void findReturnsNotFoundForMissingNotice() {
        when(recruitmentInfoDao.selectByRecordUuid("missing-record"))
                .thenReturn(recruitment("missing-record", RecruitmentStatus.PENDING_RETEST));
        when(offerNoticeDao.selectByRecordUuid("missing-record")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> offerNoticeService.findByRecordUuid("missing-record"));

        assertEquals(404, exception.getStatus().value());
    }

    private static RecruitmentInfoPo recruitment(String recordUuid, RecruitmentStatus status) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        return result;
    }

    private static OfferNoticePo notice(Long id, String recordUuid, String email, NoticeStatus status) {
        OfferNoticePo result = new OfferNoticePo();
        result.setId(id);
        result.setRecordUuid(recordUuid);
        result.setRecipientEmail(email);
        result.setStatus(status.getCode());
        result.setDraftedAt(LocalDateTime.now());
        result.setSentAt(status == NoticeStatus.SENT ? LocalDateTime.now() : null);
        return result;
    }
}
