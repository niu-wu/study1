package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RecruitmentStatusHistoryDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentStatusHistoryPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentStatusHistoryVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 招聘状态流转业务实现。 */
@Service
public class RecruitmentStatusServiceImpl implements RecruitmentStatusService {

    private static final Set<RecruitmentStatus> ACTIVE_STATUSES = Collections.unmodifiableSet(
            EnumSet.of(RecruitmentStatus.PENDING_INITIAL, RecruitmentStatus.RETEST_REVIEW,
                    RecruitmentStatus.PENDING_RETEST, RecruitmentStatus.PENDING_ONBOARDING));

    private static final Map<RecruitmentStatus, Map<StatusTransitionAction, RecruitmentStatus>>
            TRANSITION_RULES = createTransitionRules();

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final RecruitmentStatusHistoryDao recruitmentStatusHistoryDao;

    public RecruitmentStatusServiceImpl(RecruitmentInfoDao recruitmentInfoDao,
                                        RecruitmentStatusHistoryDao recruitmentStatusHistoryDao) {
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.recruitmentStatusHistoryDao = recruitmentStatusHistoryDao;
    }

    @Override
    @Transactional
    public RecruitmentInfoVO transition(String recordUuid, RecruitmentStatusTransitionDTO request,
                                        Integer operatorUserId) {
        validateTransitionRequest(recordUuid, request, operatorUserId);
        RecruitmentInfoPo current = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        if (current == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        RecruitmentStatus currentStatus = RecruitmentStatus.fromCode(current.getStatus());
        if (currentStatus == null) {
            throw ApiException.internalServerError("招聘记录状态无效");
        }
        RecruitmentStatus targetStatus = resolveTargetStatus(currentStatus, request.getAction());
        if (recruitmentInfoDao.updateStatusIfCurrent(recordUuid, currentStatus.getCode(),
                targetStatus.getCode()) != 1) {
            throw ApiException.conflict("招聘记录状态已发生变化，请刷新后重试");
        }

        RecruitmentStatusHistoryPo history = new RecruitmentStatusHistoryPo();
        history.setRecordUuid(recordUuid);
        history.setFromStatus(currentStatus.getCode());
        history.setToStatus(targetStatus.getCode());
        history.setAction(request.getAction().getCode());
        history.setOperatorUserId(operatorUserId);
        history.setRemark(request.getRemark());
        history.setCreatedAt(LocalDateTime.now());
        if (recruitmentStatusHistoryDao.insert(history) != 1) {
            throw ApiException.internalServerError("招聘状态历史保存失败");
        }

        RecruitmentInfoPo updated = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        if (updated == null) {
            throw ApiException.internalServerError("招聘信息读取失败");
        }
        return toVo(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentStatusHistoryVO> findHistory(String recordUuid) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        if (recruitmentInfoDao.selectByRecordUuid(recordUuid) == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return recruitmentStatusHistoryDao.selectByRecordUuid(recordUuid).stream()
                .map(RecruitmentStatusServiceImpl::toHistoryVo)
                .collect(Collectors.toList());
    }

    private static RecruitmentStatus resolveTargetStatus(RecruitmentStatus currentStatus,
                                                         StatusTransitionAction action) {
        RecruitmentStatus targetStatus = TRANSITION_RULES.getOrDefault(currentStatus,
                Collections.emptyMap()).get(action);
        if (targetStatus == null) {
            throw ApiException.unprocessableEntity("当前招聘状态不允许执行该动作");
        }
        return targetStatus;
    }

    private static Map<RecruitmentStatus, Map<StatusTransitionAction, RecruitmentStatus>>
    createTransitionRules() {
        EnumMap<RecruitmentStatus, Map<StatusTransitionAction, RecruitmentStatus>> rules =
                new EnumMap<>(RecruitmentStatus.class);
        addRule(rules, RecruitmentStatus.PENDING_INITIAL,
                StatusTransitionAction.SUBMIT_RETEST_REVIEW, RecruitmentStatus.RETEST_REVIEW);
        addRule(rules, RecruitmentStatus.RETEST_REVIEW,
                StatusTransitionAction.CONFIRM_RETEST, RecruitmentStatus.PENDING_RETEST);
        addRule(rules, RecruitmentStatus.PENDING_RETEST,
                StatusTransitionAction.COMPLETE_RETEST, RecruitmentStatus.PENDING_ONBOARDING);
        addRule(rules, RecruitmentStatus.PENDING_ONBOARDING,
                StatusTransitionAction.COMPLETE_ONBOARDING, RecruitmentStatus.ONBOARDED);
        for (RecruitmentStatus status : ACTIVE_STATUSES) {
            addRule(rules, status, StatusTransitionAction.REJECT, RecruitmentStatus.REJECTED);
            addRule(rules, status, StatusTransitionAction.DECLINE, RecruitmentStatus.DECLINED);
        }
        return Collections.unmodifiableMap(rules);
    }

    private static void addRule(
            Map<RecruitmentStatus, Map<StatusTransitionAction, RecruitmentStatus>> rules,
            RecruitmentStatus from, StatusTransitionAction action, RecruitmentStatus to) {
        Map<StatusTransitionAction, RecruitmentStatus> actions =
                new EnumMap<>(rules.getOrDefault(from, new EnumMap<>(StatusTransitionAction.class)));
        actions.put(action, to);
        rules.put(from, Collections.unmodifiableMap(actions));
    }

    private static void validateTransitionRequest(String recordUuid,
                                                   RecruitmentStatusTransitionDTO request,
                                                   Integer operatorUserId) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        if (request == null || request.getAction() == null) {
            throw ApiException.badRequest("状态动作不能为空");
        }
        if (operatorUserId == null || operatorUserId <= 0) {
            throw ApiException.unauthorized("未登录");
        }
    }

    private static RecruitmentInfoVO toVo(RecruitmentInfoPo source) {
        RecruitmentInfoVO result = new RecruitmentInfoVO();
        result.setRecordUuid(source.getRecordUuid());
        result.setId(source.getId());
        result.setApplicantName(source.getApplicantName());
        result.setGender(source.getGender());
        result.setPosition(source.getPosition());
        result.setPhone(source.getPhone());
        result.setEmail(source.getEmail());
        result.setApplicationChannel(source.getApplicationChannel());
        result.setApplicationMethod(source.getApplicationMethod());
        result.setStatus(source.getStatus());
        result.setInitialContactPerson(source.getInitialContactPerson());
        result.setInitialInterviewTime(source.getInitialInterviewTime());
        result.setRetestContactPerson(source.getRetestContactPerson());
        result.setRetestInterviewTime(source.getRetestInterviewTime());
        result.setCreatedAt(source.getCreatedAt());
        result.setUpdatedAt(source.getUpdatedAt());
        return result;
    }

    private static RecruitmentStatusHistoryVO toHistoryVo(RecruitmentStatusHistoryPo source) {
        RecruitmentStatusHistoryVO result = new RecruitmentStatusHistoryVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setFromStatus(source.getFromStatus());
        result.setToStatus(source.getToStatus());
        result.setAction(source.getAction());
        result.setOperatorUserId(source.getOperatorUserId());
        result.setRemark(source.getRemark());
        result.setCreatedAt(source.getCreatedAt());
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
