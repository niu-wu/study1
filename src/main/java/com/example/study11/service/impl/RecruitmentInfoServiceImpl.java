package com.example.study11.service.impl;

import com.example.study11.common.model.PageResult;
import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RecruitmentStatusHistoryDao;
import com.example.study11.entity.dto.RecruitmentInfoCreateDTO;
import com.example.study11.entity.dto.RecruitmentInfoPageRequest;
import com.example.study11.entity.dto.RecruitmentInfoUpdateDTO;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentInfoStatisticsPo;
import com.example.study11.entity.vo.RecruitmentInfoStatisticsVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** 招聘信息业务实现。 */
@Service
public class RecruitmentInfoServiceImpl implements RecruitmentInfoService {

    private static final String DEFAULT_STATUS = "PENDING_INITIAL";

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final RecruitmentStatusHistoryDao recruitmentStatusHistoryDao;

    private final CandidateResumeDao candidateResumeDao;

    public RecruitmentInfoServiceImpl(RecruitmentInfoDao recruitmentInfoDao,
                                      RecruitmentStatusHistoryDao recruitmentStatusHistoryDao,
                                      CandidateResumeDao candidateResumeDao) {
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.recruitmentStatusHistoryDao = recruitmentStatusHistoryDao;
        this.candidateResumeDao = candidateResumeDao;
    }

    @Override
    @Transactional
    public RecruitmentInfoVO create(RecruitmentInfoCreateDTO request) {
        validateCreateRequest(request);
        RecruitmentInfoPo recruitmentInfoPo = toPo(request);
        recruitmentInfoPo.setRecordUuid(UUID.randomUUID().toString());
        recruitmentInfoPo.setStatus(DEFAULT_STATUS);
        if (recruitmentInfoDao.insert(recruitmentInfoPo) != 1) {
            throw ApiException.internalServerError("招聘信息创建失败");
        }
        return findByRecordUuid(recruitmentInfoPo.getRecordUuid());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentInfoVO> findList(String applicantName, String position, String status) {
        return recruitmentInfoDao.findList(applicantName, position, status).stream()
                .map(RecruitmentInfoServiceImpl::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RecruitmentInfoVO> findPage(RecruitmentInfoPageRequest request) {
        RecruitmentInfoPageRequest condition = request == null
                ? new RecruitmentInfoPageRequest() : request;
        validatePageRequest(condition);
        int page = condition.getPage();
        int pageSize = condition.getPageSize();
        long offset = Math.multiplyExact((long) page - 1, pageSize);
        long total = recruitmentInfoDao.countByCondition(condition);
        if (total == 0 || offset >= total) {
            return new PageResult<>(page, pageSize, total, List.of());
        }
        List<RecruitmentInfoVO> records = recruitmentInfoDao.selectPage(condition, offset, pageSize)
                .stream()
                .map(RecruitmentInfoServiceImpl::toVo)
                .collect(Collectors.toList());
        for (int index = 0; index < records.size(); index++) {
            records.get(index).setSerialNo(offset + index + 1);
        }
        return new PageResult<>(page, pageSize, total, records);
    }

    @Override
    @Transactional(readOnly = true)
    public RecruitmentInfoStatisticsVO findStatistics() {
        RecruitmentInfoStatisticsPo source = recruitmentInfoDao.selectStatistics();
        RecruitmentInfoStatisticsVO result = new RecruitmentInfoStatisticsVO();
        if (source == null) {
            result.setPendingInitial(0L);
            result.setPendingRetest(0L);
            result.setPendingOnboarding(0L);
            result.setNotPassed(0L);
            return result;
        }
        result.setPendingInitial(defaultZero(source.getPendingInitial()));
        result.setPendingRetest(defaultZero(source.getPendingRetest()));
        result.setPendingOnboarding(defaultZero(source.getPendingOnboarding()));
        result.setNotPassed(defaultZero(source.getNotPassed()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RecruitmentInfoVO findByRecordUuid(String recordUuid) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        RecruitmentInfoPo result = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        if (result == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return toVo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RecruitmentInfoVO findById(Long id) {
        if (id == null || id <= 0) {
            throw ApiException.badRequest("招聘记录编号必须为正数");
        }
        RecruitmentInfoPo result = recruitmentInfoDao.selectById(id);
        if (result == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return toVo(result);
    }

    @Override
    @Transactional
    public RecruitmentInfoVO update(String recordUuid, RecruitmentInfoUpdateDTO request) {
        validateUpdateRequest(recordUuid, request);
        RecruitmentInfoPo current = recruitmentInfoDao.selectByRecordUuid(recordUuid);
        if (current == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        RecruitmentInfoPo replacement = toPo(request);
        replacement.setRecordUuid(recordUuid);
        replacement.setId(current.getId());
        replacement.setStatus(current.getStatus());
        replacement.setCreatedAt(current.getCreatedAt());
        if (recruitmentInfoDao.updateByRecordUuid(replacement) != 1) {
            throw ApiException.internalServerError("招聘信息更新失败");
        }
        return findByRecordUuid(recordUuid);
    }

    @Override
    @Transactional
    public void delete(String recordUuid) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        if (recruitmentInfoDao.selectByRecordUuidForUpdate(recordUuid) == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        if (recruitmentStatusHistoryDao.countByRecordUuid(recordUuid) > 0) {
            throw ApiException.conflict("招聘记录已有状态历史，不能删除");
        }
        if (candidateResumeDao.countByRecordUuid(recordUuid) > 0) {
            throw ApiException.conflict("招聘记录已有简历附件，不能删除");
        }
        if (recruitmentInfoDao.deleteByRecordUuid(recordUuid) != 1) {
            throw ApiException.internalServerError("招聘信息删除失败");
        }
    }

    private static void validateCreateRequest(RecruitmentInfoCreateDTO request) {
        if (request == null) {
            throw ApiException.badRequest("招聘信息不能为空");
        }
        if (isBlank(request.getApplicantName()) || isBlank(request.getPosition())) {
            throw ApiException.badRequest("应聘人姓名和岗位不能为空");
        }
    }

    private static void validateUpdateRequest(String recordUuid, RecruitmentInfoUpdateDTO request) {
        if (isBlank(recordUuid)) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        if (request == null || isBlank(request.getApplicantName()) || isBlank(request.getPosition())) {
            throw ApiException.badRequest("招聘信息、应聘人姓名和岗位不能为空");
        }
    }

    private static RecruitmentInfoPo toPo(RecruitmentInfoCreateDTO request) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setApplicantName(request.getApplicantName());
        result.setGender(request.getGender());
        result.setPosition(request.getPosition());
        result.setPhone(request.getPhone());
        result.setEmail(request.getEmail());
        result.setApplicationChannel(request.getApplicationChannel());
        result.setApplicationMethod(request.getApplicationMethod());
        result.setInitialContactPerson(request.getInitialContactPerson());
        result.setInitialInterviewTime(request.getInitialInterviewTime());
        result.setRetestContactPerson(request.getRetestContactPerson());
        result.setRetestInterviewTime(request.getRetestInterviewTime());
        return result;
    }

    private static RecruitmentInfoPo toPo(RecruitmentInfoUpdateDTO request) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setApplicantName(request.getApplicantName());
        result.setGender(request.getGender());
        result.setPosition(request.getPosition());
        result.setPhone(request.getPhone());
        result.setEmail(request.getEmail());
        result.setApplicationChannel(request.getApplicationChannel());
        result.setApplicationMethod(request.getApplicationMethod());
        result.setInitialContactPerson(request.getInitialContactPerson());
        result.setInitialInterviewTime(request.getInitialInterviewTime());
        result.setRetestContactPerson(request.getRetestContactPerson());
        result.setRetestInterviewTime(request.getRetestInterviewTime());
        return result;
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

    private static void validatePageRequest(RecruitmentInfoPageRequest request) {
        if (request.getPage() == null || request.getPage() < 1) {
            throw ApiException.badRequest("页码必须大于等于1");
        }
        if (request.getPageSize() == null || request.getPageSize() < 1 || request.getPageSize() > 100) {
            throw ApiException.badRequest("每页数量必须在1到100之间");
        }
        if (request.getCreatedFrom() != null && request.getCreatedTo() != null
                && request.getCreatedFrom().isAfter(request.getCreatedTo())) {
            throw ApiException.badRequest("创建时间范围无效");
        }
    }

    private static long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
