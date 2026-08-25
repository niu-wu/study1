package com.example.study11.service.impl;

import com.example.study11.dao.OnboardingRecordDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.OnboardingRecordPo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.OnboardingRecordVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.OnboardingService;
import com.example.study11.service.RecruitmentStatusService;
import com.example.study11.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 入职业务实现，复用 study1-1 user 表和 BCrypt 用户服务。 */
@Service
public class OnboardingServiceImpl implements OnboardingService {

    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static final int INITIAL_PASSWORD_LENGTH = 8;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final OnboardingRecordDao onboardingRecordDao;

    private final UserService userService;

    private final RecruitmentStatusService recruitmentStatusService;

    public OnboardingServiceImpl(RecruitmentInfoDao recruitmentInfoDao,
                                 OnboardingRecordDao onboardingRecordDao,
                                 UserService userService,
                                 RecruitmentStatusService recruitmentStatusService) {
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.onboardingRecordDao = onboardingRecordDao;
        this.userService = userService;
        this.recruitmentStatusService = recruitmentStatusService;
    }

    @Override
    @Transactional
    public OnboardingRecordVO process(String recordUuid, LocalDate onboardingDate,
                                      String onboardingNote, Integer processedByUserId) {
        validateRecordUuid(recordUuid);
        validateOperator(processedByUserId);
        RecruitmentInfoPo recruitment = lockRecruitment(recordUuid);
        if (onboardingRecordDao.selectByRecordUuid(recordUuid) != null) {
            throw ApiException.conflict("该招聘记录已办理入职");
        }
        if (parseStatus(recruitment) != RecruitmentStatus.PENDING_ONBOARDING) {
            throw ApiException.unprocessableEntity("当前招聘状态不允许办理入职");
        }
        String phone = trimToNull(recruitment.getPhone());
        if (phone == null) {
            throw ApiException.badRequest("候选人手机号不能为空，无法创建账号");
        }
        UserPo existingUser = userService.getByUserName(phone);
        if (existingUser != null) {
            throw ApiException.conflict("手机号已存在对应用户账号");
        }

        String initialPassword = generateInitialPassword();
        UserSaveDTO userRequest = new UserSaveDTO();
        userRequest.setUsername(phone);
        userRequest.setPassword(initialPassword);
        userRequest.setPhone(phone);
        userRequest.setEmail(trimToNull(recruitment.getEmail()));
        Integer userId;
        try {
            userId = userService.dealSave(userRequest);
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("手机号已存在对应用户账号");
        }
        if (userId == null || userId <= 0) {
            throw ApiException.internalServerError("系统用户创建失败");
        }

        LocalDate actualOnboardingDate = onboardingDate == null ? LocalDate.now() : onboardingDate;
        LocalDateTime now = LocalDateTime.now();
        OnboardingRecordPo record = new OnboardingRecordPo();
        record.setRecordUuid(recordUuid.trim());
        record.setUserId(userId);
        record.setOnboardingDate(actualOnboardingDate);
        record.setOnboardingNote(trimToNull(onboardingNote));
        record.setProcessedByUserId(processedByUserId);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        try {
            if (onboardingRecordDao.insert(record) != 1) {
                throw ApiException.internalServerError("入职记录保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("该招聘记录已办理入职");
        }

        RecruitmentStatusTransitionDTO transition = new RecruitmentStatusTransitionDTO();
        transition.setAction(StatusTransitionAction.COMPLETE_ONBOARDING);
        transition.setRemark("办理入职");
        RecruitmentInfoVO transitioned = recruitmentStatusService.transition(recordUuid, transition,
                processedByUserId);

        OnboardingRecordVO result = toVo(record, transitioned.getStatus(), phone);
        result.setInitialPassword(initialPassword);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingRecordVO findByRecordUuid(String recordUuid) {
        validateRecordUuid(recordUuid);
        RecruitmentInfoPo recruitment = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        if (recruitment == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        OnboardingRecordPo record = onboardingRecordDao.selectByRecordUuid(recordUuid);
        if (record == null) {
            throw ApiException.notFound("入职记录不存在");
        }
        UserPo user = userService.getByUserName(recruitment.getPhone());
        String username = user == null ? null : user.getUsername();
        return toVo(record, recruitment.getStatus(), username);
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

    private static OnboardingRecordVO toVo(OnboardingRecordPo source, String status, String username) {
        OnboardingRecordVO result = new OnboardingRecordVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setStatus(status);
        result.setUserId(source.getUserId());
        result.setUsername(username);
        result.setOnboardingDate(source.getOnboardingDate());
        result.setOnboardingNote(source.getOnboardingNote());
        result.setProcessedByUserId(source.getProcessedByUserId());
        result.setCreatedAt(source.getCreatedAt());
        result.setUpdatedAt(source.getUpdatedAt());
        return result;
    }

    private static String generateInitialPassword() {
        StringBuilder result = new StringBuilder(INITIAL_PASSWORD_LENGTH);
        for (int index = 0; index < INITIAL_PASSWORD_LENGTH; index++) {
            result.append(PASSWORD_ALPHABET.charAt(SECURE_RANDOM.nextInt(PASSWORD_ALPHABET.length())));
        }
        return result.toString();
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

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
