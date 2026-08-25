package com.example.study11.service.impl;

import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RecruitmentRejectionDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.enums.TalentCategory;
import com.example.study11.entity.po.CandidateResumePo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentRejectionPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentRejectionVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import com.example.study11.service.RejectionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** 淘汰、放弃入职和人才库业务实现。 */
@Service
public class RejectionServiceImpl implements RejectionService {

    private static final String REJECT_ACTION = "REJECT";

    private static final String DECLINE_ACTION = "DECLINE";

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final RecruitmentRejectionDao recruitmentRejectionDao;

    private final CandidateResumeDao candidateResumeDao;

    private final RecruitmentStatusService recruitmentStatusService;

    public RejectionServiceImpl(RecruitmentInfoDao recruitmentInfoDao,
                                RecruitmentRejectionDao recruitmentRejectionDao,
                                CandidateResumeDao candidateResumeDao,
                                RecruitmentStatusService recruitmentStatusService) {
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.recruitmentRejectionDao = recruitmentRejectionDao;
        this.candidateResumeDao = candidateResumeDao;
        this.recruitmentStatusService = recruitmentStatusService;
    }

    @Override
    @Transactional
    public RecruitmentRejectionVO reject(String recordUuid, RejectionStage rejectionStage,
                                         RejectionReason rejectionReason, TalentCategory talentCategory,
                                         String remark, Long resumeAttachmentId, Integer operatorUserId) {
        validateCommon(recordUuid, talentCategory, operatorUserId);
        if (rejectionStage == null) {
            throw ApiException.badRequest("淘汰阶段不能为空");
        }
        if (rejectionReason == null) {
            throw ApiException.badRequest("淘汰原因不能为空");
        }
        RecruitmentInfoPo recruitment = lockRecruitment(recordUuid);
        ensureNoExisting(recordUuid);
        ensureActive(recruitment);
        validateAttachment(recordUuid, resumeAttachmentId);

        RecruitmentRejectionPo rejection = buildRejection(recordUuid, REJECT_ACTION,
                rejectionStage, rejectionReason, talentCategory, remark, resumeAttachmentId, operatorUserId);
        insert(rejection);
        RecruitmentInfoVO transitioned = transition(recordUuid, StatusTransitionAction.REJECT,
                trimToNull(remark) == null ? "淘汰候选人" : trimToNull(remark), operatorUserId);
        return toVo(rejection, transitioned.getStatus());
    }

    @Override
    @Transactional
    public RecruitmentRejectionVO decline(String recordUuid, TalentCategory talentCategory,
                                          String remark, Integer operatorUserId) {
        validateCommon(recordUuid, talentCategory, operatorUserId);
        RecruitmentInfoPo recruitment = lockRecruitment(recordUuid);
        ensureNoExisting(recordUuid);
        RecruitmentStatus status = parseStatus(recruitment);
        if (status != RecruitmentStatus.PENDING_ONBOARDING) {
            throw ApiException.unprocessableEntity("当前招聘状态不允许放弃入职");
        }

        RecruitmentRejectionPo rejection = buildRejection(recordUuid, DECLINE_ACTION,
                RejectionStage.ONBOARDING, RejectionReason.CANDIDATE_DECLINED,
                talentCategory, remark, null, operatorUserId);
        insert(rejection);
        RecruitmentInfoVO transitioned = transition(recordUuid, StatusTransitionAction.DECLINE,
                trimToNull(remark) == null ? "候选人主动放弃" : trimToNull(remark), operatorUserId);
        return toVo(rejection, transitioned.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentRejectionVO> findTalentPool(TalentCategory talentCategory,
                                                       RejectionReason rejectionReason,
                                                       RejectionStage rejectionStage) {
        List<RecruitmentRejectionPo> records = recruitmentRejectionDao.selectTalentPool(
                codeOf(talentCategory), codeOf(rejectionReason), codeOf(rejectionStage));
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(record -> toVo(record, null)).toList();
    }

    private void ensureNoExisting(String recordUuid) {
        if (recruitmentRejectionDao.selectByRecordUuid(recordUuid) != null) {
            throw ApiException.conflict("该招聘记录已存在淘汰或放弃入职记录");
        }
    }

    private void validateAttachment(String recordUuid, Long resumeAttachmentId) {
        if (resumeAttachmentId == null) {
            return;
        }
        CandidateResumePo resume = candidateResumeDao.selectById(resumeAttachmentId);
        if (resume == null) {
            throw ApiException.notFound("关联简历附件不存在");
        }
        if (!recordUuid.equals(resume.getRecordUuid())) {
            throw ApiException.badRequest("关联简历附件不属于当前招聘记录");
        }
    }

    private void insert(RecruitmentRejectionPo rejection) {
        try {
            if (recruitmentRejectionDao.insert(rejection) != 1) {
                throw ApiException.internalServerError("淘汰记录保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("该招聘记录已存在淘汰或放弃入职记录");
        }
    }

    private RecruitmentInfoVO transition(String recordUuid, StatusTransitionAction action,
                                         String remark, Integer operatorUserId) {
        RecruitmentStatusTransitionDTO transition = new RecruitmentStatusTransitionDTO();
        transition.setAction(action);
        transition.setRemark(remark);
        return recruitmentStatusService.transition(recordUuid, transition, operatorUserId);
    }

    private static RecruitmentRejectionPo buildRejection(String recordUuid, String action,
                                                         RejectionStage rejectionStage,
                                                         RejectionReason rejectionReason,
                                                         TalentCategory talentCategory,
                                                         String remark, Long resumeAttachmentId,
                                                         Integer operatorUserId) {
        LocalDateTime now = LocalDateTime.now();
        RecruitmentRejectionPo result = new RecruitmentRejectionPo();
        result.setRecordUuid(recordUuid.trim());
        result.setAction(action);
        result.setRejectionStage(rejectionStage.getCode());
        result.setRejectionReason(rejectionReason.getCode());
        result.setRemark(trimToNull(remark));
        result.setResumeAttachmentId(resumeAttachmentId);
        result.setTalentCategory(talentCategory.getCode());
        result.setOperatorUserId(operatorUserId);
        result.setRejectionTime(now);
        result.setCreatedAt(now);
        return result;
    }

    private RecruitmentInfoPo lockRecruitment(String recordUuid) {
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuidForUpdate(recordUuid);
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return recruitment;
    }

    private static void ensureActive(RecruitmentInfoPo recruitment) {
        if (parseStatus(recruitment).isTerminal()) {
            throw ApiException.unprocessableEntity("终态招聘记录不能再次处理");
        }
    }

    private static RecruitmentStatus parseStatus(RecruitmentInfoPo recruitment) {
        RecruitmentStatus status = RecruitmentStatus.fromCode(recruitment.getStatus());
        if (status == null) {
            throw ApiException.internalServerError("招聘记录状态无效");
        }
        return status;
    }

    private static void validateCommon(String recordUuid, TalentCategory talentCategory,
                                       Integer operatorUserId) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        if (talentCategory == null) {
            throw ApiException.badRequest("人才库分类不能为空");
        }
        if (operatorUserId == null || operatorUserId <= 0) {
            throw ApiException.unauthorized("当前登录用户无效");
        }
    }

    private static RecruitmentRejectionVO toVo(RecruitmentRejectionPo source, String status) {
        RecruitmentRejectionVO result = new RecruitmentRejectionVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setAction(source.getAction());
        result.setRejectionStage(enumCode(source.getRejectionStage(), RejectionStage.values()));
        result.setRejectionReason(enumCode(source.getRejectionReason(), RejectionReason.values()));
        result.setRemark(source.getRemark());
        result.setResumeAttachmentId(source.getResumeAttachmentId());
        result.setTalentCategory(enumCode(source.getTalentCategory(), TalentCategory.values()));
        result.setOperatorUserId(source.getOperatorUserId());
        result.setRejectionTime(source.getRejectionTime());
        result.setCreatedAt(source.getCreatedAt());
        result.setStatus(status);
        return result;
    }

    private static <E extends Enum<E>> E enumCode(String code, E[] values) {
        if (code == null) {
            return null;
        }
        for (E value : values) {
            if (value.name().equals(code)) {
                return value;
            }
        }
        return null;
    }

    private static String codeOf(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
