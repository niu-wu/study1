package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RetestApplicationDao;
import com.example.study11.dao.RetestReviewDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.RecruitmentType;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RetestApplicationPo;
import com.example.study11.entity.po.RetestReviewPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RetestApplicationVO;
import com.example.study11.entity.vo.RetestDetailsVO;
import com.example.study11.entity.vo.RetestReviewVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import com.example.study11.service.RetestService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 复试申请与安排确认业务实现。 */
@Service
public class RetestServiceImpl implements RetestService {

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final RetestApplicationDao retestApplicationDao;

    private final RetestReviewDao retestReviewDao;

    private final RecruitmentStatusService recruitmentStatusService;

    public RetestServiceImpl(RecruitmentInfoDao recruitmentInfoDao,
                             RetestApplicationDao retestApplicationDao,
                             RetestReviewDao retestReviewDao,
                             RecruitmentStatusService recruitmentStatusService) {
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.retestApplicationDao = retestApplicationDao;
        this.retestReviewDao = retestReviewDao;
        this.recruitmentStatusService = recruitmentStatusService;
    }

    @Override
    @Transactional
    public RetestDetailsVO apply(String recordUuid, String applicantRemark, Integer applicantUserId) {
        validateRecordUuid(recordUuid);
        validateOperator(applicantUserId);
        RecruitmentInfoPo recruitment = lockRecruitment(recordUuid);
        if (retestApplicationDao.selectByRecordUuid(recordUuid) != null) {
            throw ApiException.conflict("该招聘记录已申请复试");
        }
        RecruitmentStatus status = parseStatus(recruitment);
        if (status != RecruitmentStatus.PENDING_INITIAL) {
            throw ApiException.unprocessableEntity("当前招聘状态不允许申请复试");
        }

        RetestApplicationPo application = new RetestApplicationPo();
        application.setRecordUuid(recordUuid);
        application.setApplicantRemark(applicantRemark);
        application.setApplicantUserId(applicantUserId);
        application.setApplicationTime(LocalDateTime.now());
        application.setCreatedAt(application.getApplicationTime());
        try {
            if (retestApplicationDao.insert(application) != 1) {
                throw ApiException.internalServerError("复试申请保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("该招聘记录已申请复试");
        }

        RecruitmentStatusTransitionDTO transition = new RecruitmentStatusTransitionDTO();
        transition.setAction(StatusTransitionAction.SUBMIT_RETEST_REVIEW);
        transition.setRemark(applicantRemark);
        RecruitmentInfoVO transitioned = recruitmentStatusService.transition(recordUuid, transition,
                applicantUserId);
        return details(recordUuid, transitioned.getStatus(), transitioned.getRecruitmentType());
    }

    @Override
    @Transactional
    public RetestDetailsVO confirm(String recordUuid, String retestCompany, String retestContactPerson,
                                   LocalDateTime retestTime, Integer reviewerUserId) {
        validateRecordUuid(recordUuid);
        validateOperator(reviewerUserId);
        RecruitmentInfoPo recruitment = lockRecruitment(recordUuid);
        RecruitmentStatus status = parseStatus(recruitment);
        if (status != RecruitmentStatus.RETEST_REVIEW) {
            throw ApiException.unprocessableEntity("当前招聘状态不允许确认复试");
        }
        if (retestApplicationDao.selectByRecordUuid(recordUuid) == null) {
            throw ApiException.conflict("该招聘记录尚未申请复试");
        }
        if (retestReviewDao.selectByRecordUuid(recordUuid) != null) {
            throw ApiException.conflict("该招聘记录已确认复试安排");
        }
        if (recruitment.getRecruitmentType() == RecruitmentType.OUTSOURCED
                && (isBlank(retestCompany) || isBlank(retestContactPerson) || retestTime == null)) {
            throw ApiException.badRequest("外派招聘的复试公司、对接人和时间不能为空");
        }

        RetestReviewPo review = new RetestReviewPo();
        review.setRecordUuid(recordUuid);
        review.setRetestCompany(trimToNull(retestCompany));
        review.setRetestContactPerson(trimToNull(retestContactPerson));
        review.setRetestTime(retestTime);
        review.setReviewerUserId(reviewerUserId);
        review.setReviewTime(LocalDateTime.now());
        review.setCreatedAt(review.getReviewTime());
        try {
            if (retestReviewDao.insert(review) != 1) {
                throw ApiException.internalServerError("复试安排保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("该招聘记录已确认复试安排");
        }

        RecruitmentStatusTransitionDTO transition = new RecruitmentStatusTransitionDTO();
        transition.setAction(StatusTransitionAction.CONFIRM_RETEST);
        transition.setRemark("确认复试安排");
        RecruitmentInfoVO transitioned = recruitmentStatusService.transition(recordUuid, transition,
                reviewerUserId);
        return details(recordUuid, transitioned.getStatus(), transitioned.getRecruitmentType());
    }

    @Override
    @Transactional(readOnly = true)
    public RetestDetailsVO findByRecordUuid(String recordUuid) {
        validateRecordUuid(recordUuid);
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return details(recordUuid, recruitment.getStatus(), recruitment.getRecruitmentType());
    }

    private RetestDetailsVO details(String recordUuid, String status, RecruitmentType recruitmentType) {
        RetestDetailsVO result = new RetestDetailsVO();
        result.setRecordUuid(recordUuid);
        result.setStatus(status);
        result.setRecruitmentType(recruitmentType);
        RetestApplicationPo application = retestApplicationDao.selectByRecordUuid(recordUuid);
        if (application != null) {
            result.setApplication(toApplicationVo(application));
        }
        RetestReviewPo review = retestReviewDao.selectByRecordUuid(recordUuid);
        if (review != null) {
            result.setReview(toReviewVo(review));
        }
        return result;
    }

    private RecruitmentInfoPo lockRecruitment(String recordUuid) {
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuidForUpdate(recordUuid);
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return recruitment;
    }

    private static RecruitmentStatus parseStatus(RecruitmentInfoPo recruitment) {
        RecruitmentStatus status = RecruitmentStatus.fromCode(recruitment.getStatus());
        if (status == null) {
            throw ApiException.internalServerError("招聘记录状态无效");
        }
        return status;
    }

    private static void validateRecordUuid(String recordUuid) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
    }

    private static void validateOperator(Integer operatorUserId) {
        if (operatorUserId == null || operatorUserId <= 0) {
            throw ApiException.unauthorized("当前登录用户无效");
        }
    }

    private static RetestApplicationVO toApplicationVo(RetestApplicationPo source) {
        RetestApplicationVO result = new RetestApplicationVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setApplicationTime(source.getApplicationTime());
        result.setApplicantRemark(source.getApplicantRemark());
        result.setApplicantUserId(source.getApplicantUserId());
        result.setCreatedAt(source.getCreatedAt());
        return result;
    }

    private static RetestReviewVO toReviewVo(RetestReviewPo source) {
        RetestReviewVO result = new RetestReviewVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setRetestCompany(source.getRetestCompany());
        result.setRetestContactPerson(source.getRetestContactPerson());
        result.setRetestTime(source.getRetestTime());
        result.setReviewerUserId(source.getReviewerUserId());
        result.setReviewTime(source.getReviewTime());
        result.setCreatedAt(source.getCreatedAt());
        return result;
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
