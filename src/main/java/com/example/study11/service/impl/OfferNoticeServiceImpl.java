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
import com.example.study11.exception.ApiException;
import com.example.study11.service.OfferNoticeService;
import com.example.study11.service.RecruitmentStatusService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 录用通知业务实现。第一阶段只记录模拟发送状态，不调用 SMTP。 */
@Service
public class OfferNoticeServiceImpl implements OfferNoticeService {

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final OfferNoticeDao offerNoticeDao;

    private final RecruitmentStatusService recruitmentStatusService;

    public OfferNoticeServiceImpl(RecruitmentInfoDao recruitmentInfoDao,
                                  OfferNoticeDao offerNoticeDao,
                                  RecruitmentStatusService recruitmentStatusService) {
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.offerNoticeDao = offerNoticeDao;
        this.recruitmentStatusService = recruitmentStatusService;
    }

    @Override
    @Transactional
    public OfferNoticeVO createDraft(String recordUuid, String recipientEmail, String noticeContent,
                                     Integer draftedByUserId) {
        validateRecordUuid(recordUuid);
        validateUserId(draftedByUserId);
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        validateRecruitmentForDraft(recruitment);
        if (offerNoticeDao.selectByRecordUuid(recordUuid) != null) {
            throw ApiException.conflict("该招聘记录已存在录用通知");
        }

        OfferNoticePo notice = new OfferNoticePo();
        notice.setRecordUuid(recordUuid.trim());
        notice.setRecipientEmail(trimToNull(recipientEmail));
        notice.setNoticeContent(trimToNull(noticeContent));
        notice.setStatus(NoticeStatus.DRAFT.getCode());
        notice.setDraftedByUserId(draftedByUserId);
        notice.setDraftedAt(LocalDateTime.now());
        try {
            if (offerNoticeDao.insert(notice) != 1) {
                throw ApiException.internalServerError("录用通知草稿保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("该招聘记录已存在录用通知");
        }
        OfferNoticePo saved = offerNoticeDao.selectByRecordUuid(recordUuid);
        return toVo(saved == null ? notice : saved);
    }

    @Override
    @Transactional
    public OfferNoticeVO send(String recordUuid, Integer senderUserId) {
        validateRecordUuid(recordUuid);
        validateUserId(senderUserId);
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuidForUpdate(recordUuid);
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        OfferNoticePo notice = offerNoticeDao.selectByRecordUuidForUpdate(recordUuid);
        if (notice == null) {
            throw ApiException.notFound("录用通知草稿不存在");
        }
        NoticeStatus noticeStatus = NoticeStatus.fromCode(notice.getStatus());
        if (noticeStatus == NoticeStatus.SENT) {
            throw ApiException.conflict("录用通知已经发送");
        }
        if (noticeStatus != NoticeStatus.DRAFT) {
            throw ApiException.internalServerError("录用通知状态无效");
        }
        if (isBlank(notice.getRecipientEmail())) {
            throw ApiException.badRequest("收件邮箱不能为空");
        }
        RecruitmentStatus recruitmentStatus = RecruitmentStatus.fromCode(recruitment.getStatus());
        if (recruitmentStatus != RecruitmentStatus.PENDING_RETEST) {
            throw ApiException.unprocessableEntity("复试尚未完成，暂不能发送录用通知");
        }

        RecruitmentStatusTransitionDTO transition = new RecruitmentStatusTransitionDTO();
        transition.setAction(StatusTransitionAction.COMPLETE_RETEST);
        transition.setRemark("发送录用通知");
        recruitmentStatusService.transition(recordUuid, transition, senderUserId);

        LocalDateTime sentAt = LocalDateTime.now();
        if (offerNoticeDao.updateToSent(recordUuid, senderUserId, sentAt) != 1) {
            throw ApiException.conflict("录用通知状态已发生变化，请刷新后重试");
        }
        OfferNoticePo saved = offerNoticeDao.selectByRecordUuid(recordUuid);
        if (saved == null) {
            notice.setStatus(NoticeStatus.SENT.getCode());
            notice.setSentByUserId(senderUserId);
            notice.setSentAt(sentAt);
            saved = notice;
        }
        return toVo(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferNoticeVO findByRecordUuid(String recordUuid) {
        validateRecordUuid(recordUuid);
        if (recruitmentInfoDao.selectByRecordUuid(recordUuid) == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        OfferNoticePo notice = offerNoticeDao.selectByRecordUuid(recordUuid);
        if (notice == null) {
            throw ApiException.notFound("录用通知不存在");
        }
        return toVo(notice);
    }

    private static void validateRecordUuid(String recordUuid) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
    }

    private static void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw ApiException.unauthorized("当前登录用户无效");
        }
    }

    private static void validateRecruitmentForDraft(RecruitmentInfoPo recruitment) {
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        RecruitmentStatus status = RecruitmentStatus.fromCode(recruitment.getStatus());
        if (status == null) {
            throw ApiException.internalServerError("招聘记录状态无效");
        }
        if (status.isTerminal()) {
            throw ApiException.unprocessableEntity("终态招聘记录不能创建录用通知");
        }
    }

    private static OfferNoticeVO toVo(OfferNoticePo source) {
        OfferNoticeVO result = new OfferNoticeVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setRecipientEmail(source.getRecipientEmail());
        result.setNoticeContent(source.getNoticeContent());
        result.setStatus(NoticeStatus.fromCode(source.getStatus()));
        result.setDraftedByUserId(source.getDraftedByUserId());
        result.setDraftedAt(source.getDraftedAt());
        result.setSentByUserId(source.getSentByUserId());
        result.setSentAt(source.getSentAt());
        result.setCreatedAt(source.getCreatedAt());
        result.setUpdatedAt(source.getUpdatedAt());
        return result;
    }

    private static String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
