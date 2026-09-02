package com.example.study11.service.impl;

import com.example.study11.common.model.PageResult;
import com.example.study11.dao.RecruitmentJobDao;
import com.example.study11.dao.RecruitmentJobStatusHistoryDao;
import com.example.study11.dao.RecruitmentJobCompanyDao;
import com.example.study11.entity.dto.RecruitmentJobCreateDTO;
import com.example.study11.entity.dto.RecruitmentJobPageRequest;
import com.example.study11.entity.dto.RecruitmentJobStatusTransitionDTO;
import com.example.study11.entity.dto.RecruitmentJobUpdateDTO;
import com.example.study11.entity.dto.RecruitmentAllocationStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentJobStatus;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.entity.po.RecruitmentJobPo;
import com.example.study11.entity.po.RecruitmentJobStatusHistoryPo;
import com.example.study11.entity.po.RecruitmentJobCompanyPo;
import com.example.study11.entity.vo.RecruitmentJobVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentJobService;
import com.example.study11.service.RoleAuthorizationService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** 招聘岗位业务实现。 */
@Service
public class RecruitmentJobServiceImpl implements RecruitmentJobService {

    private static final String DEFAULT_STATUS = "OPEN";

    private final RecruitmentJobDao recruitmentJobDao;
    private final RoleAuthorizationService roleAuthorizationService;
    private final RecruitmentJobStatusHistoryDao statusHistoryDao;
    private final RecruitmentJobCompanyDao jobCompanyDao;

    public RecruitmentJobServiceImpl(RecruitmentJobDao recruitmentJobDao,
                                     RoleAuthorizationService roleAuthorizationService,
                                     RecruitmentJobStatusHistoryDao statusHistoryDao,
                                     RecruitmentJobCompanyDao jobCompanyDao) {
        this.recruitmentJobDao = recruitmentJobDao;
        this.roleAuthorizationService = roleAuthorizationService;
        this.statusHistoryDao = statusHistoryDao;
        this.jobCompanyDao = jobCompanyDao;
    }

    @Override
    @Transactional
    public RecruitmentJobVO create(RecruitmentJobCreateDTO request, Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        validateSalary(request);
        RecruitmentJobPo job = toPo(request);
        job.setJobUuid(UUID.randomUUID().toString());
        job.setStatus(DEFAULT_STATUS);
        job.setIsDeleted(0);
        job.setCreatedByUserId(operatorUserId);
        job.setUpdatedByUserId(operatorUserId);
        job.setVersion(0);
        if (recruitmentJobDao.insert(job) != 1) {
            throw ApiException.internalServerError("岗位创建失败");
        }
        return toVo(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RecruitmentJobVO> findPage(RecruitmentJobPageRequest request, Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        RecruitmentJobPageRequest condition = request == null ? new RecruitmentJobPageRequest() : request;
        int page = condition.getPage() == null ? 1 : condition.getPage();
        int pageSize = condition.getPageSize() == null ? 20 : condition.getPageSize();
        long offset = Math.multiplyExact((long) page - 1, pageSize);
        long total = recruitmentJobDao.countByCondition(condition);
        if (total == 0 || offset >= total) {
            return new PageResult<>(page, pageSize, total, List.of());
        }
        List<RecruitmentJobVO> records = recruitmentJobDao.selectPage(condition, offset, pageSize)
                .stream().map(RecruitmentJobServiceImpl::toVo).collect(Collectors.toList());
        for (int index = 0; index < records.size(); index++) {
            records.get(index).setSerialNo(offset + index + 1);
        }
        return new PageResult<>(page, pageSize, total, records);
    }

    @Override
    @Transactional(readOnly = true)
    public RecruitmentJobVO findByJobUuid(String jobUuid, Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        RecruitmentJobPo job = existing(jobUuid);
        return toVo(job);
    }

    @Override
    @Transactional
    public RecruitmentJobVO update(String jobUuid, RecruitmentJobUpdateDTO request, Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        validateSalary(request);
        RecruitmentJobPo current = existing(jobUuid);
        RecruitmentJobPo replacement = toPo(request);
        replacement.setJobUuid(jobUuid);
        replacement.setId(current.getId());
        replacement.setStatus(current.getStatus());
        replacement.setIsDeleted(current.getIsDeleted());
        replacement.setCreatedByUserId(current.getCreatedByUserId());
        replacement.setUpdatedByUserId(operatorUserId);
        replacement.setVersion(current.getVersion());
        if (recruitmentJobDao.updateByJobUuid(replacement) != 1) {
            throw ApiException.internalServerError("岗位更新失败");
        }
        return toVo(replacement);
    }

    @Override
    @Transactional
    public RecruitmentJobVO transitionStatus(String jobUuid, RecruitmentJobStatusTransitionDTO request,
                                              Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        RecruitmentJobPo current = recruitmentJobDao.selectByJobUuidForUpdate(jobUuid);
        if (current == null || current.getIsDeleted() != null && current.getIsDeleted() != 0) {
            throw ApiException.notFound("岗位不存在");
        }
        RecruitmentJobStatus target = request.getToStatus();
        if (target == null || target.name().equals(current.getStatus())) {
            throw ApiException.conflict("岗位状态未发生变化");
        }
        if (RecruitmentJobStatus.COMPLETED.name().equals(current.getStatus())
                || (RecruitmentJobStatus.CLOSED.name().equals(current.getStatus())
                && target == RecruitmentJobStatus.COMPLETED)) {
            throw ApiException.unprocessableEntity("岗位状态不允许此操作");
        }
        if (recruitmentJobDao.updateStatusIfCurrent(jobUuid, current.getStatus(), target.name(), operatorUserId) != 1) {
            throw ApiException.conflict("岗位状态已被其他请求修改");
        }
        RecruitmentJobStatusHistoryPo history = new RecruitmentJobStatusHistoryPo();
        history.setJobUuid(jobUuid);
        history.setScope("JOB");
        history.setFromStatus(current.getStatus());
        history.setToStatus(target.name());
        history.setAction("STATUS_TRANSITION");
        history.setOperatorUserId(operatorUserId);
        history.setRemark(request.getRemark());
        if (statusHistoryDao.insert(history) != 1) {
            throw ApiException.internalServerError("岗位状态审计写入失败");
        }
        current.setStatus(target.name());
        current.setUpdatedByUserId(operatorUserId);
        return toVo(current);
    }

    @Override
    @Transactional
    public void delete(String jobUuid, Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        RecruitmentJobPo current = existing(jobUuid);
        if (current.getIsDeleted() != null && current.getIsDeleted() != 0) {
            throw ApiException.conflict("岗位已删除");
        }
        if (recruitmentJobDao.softDelete(jobUuid, operatorUserId) != 1) {
            throw ApiException.internalServerError("岗位删除失败");
        }
    }

    @Override
    @Transactional
    public void transitionAllocationStatus(String jobUuid, String allocationUuid,
                                           RecruitmentAllocationStatusTransitionDTO request,
                                           Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        if (allocationUuid == null || allocationUuid.isBlank() || request == null || request.getToStatus() == null) {
            throw ApiException.badRequest("公司配额状态参数不能为空");
        }
        RecruitmentJobCompanyPo current = jobCompanyDao.selectByUuidForUpdate(jobUuid, allocationUuid);
        if (current == null) {
            throw ApiException.notFound("公司配额不存在");
        }
        String target = request.getToStatus().name();
        if (target.equals(current.getStatus())) {
            throw ApiException.conflict("公司配额状态未发生变化");
        }
        if ("COMPLETED".equals(current.getStatus())) {
            throw ApiException.unprocessableEntity("公司配额状态不允许此操作");
        }
        if (jobCompanyDao.updateStatusIfCurrent(jobUuid, allocationUuid, current.getStatus(), target, operatorUserId) != 1) {
            throw ApiException.conflict("公司配额状态已被其他请求修改");
        }
        RecruitmentJobStatusHistoryPo history = new RecruitmentJobStatusHistoryPo();
        history.setJobUuid(jobUuid);
        history.setAllocationUuid(allocationUuid);
        history.setScope("ALLOCATION");
        history.setFromStatus(current.getStatus());
        history.setToStatus(target);
        history.setAction("STATUS_TRANSITION");
        history.setOperatorUserId(operatorUserId);
        history.setRemark(request.getRemark());
        if (statusHistoryDao.insert(history) != 1) {
            throw ApiException.internalServerError("公司配额状态审计写入失败");
        }
    }

    private RecruitmentJobPo existing(String jobUuid) {
        if (jobUuid == null || jobUuid.isBlank()) {
            throw ApiException.badRequest("岗位 UUID 不能为空");
        }
        RecruitmentJobPo job = recruitmentJobDao.selectByJobUuid(jobUuid);
        if (job == null || job.getIsDeleted() != null && job.getIsDeleted() != 0) {
            throw ApiException.notFound("岗位不存在");
        }
        return job;
    }

    private static void validateSalary(RecruitmentJobCreateDTO request) {
        if (request == null || request.getJobName() == null || request.getJobName().isBlank()) {
            throw ApiException.badRequest("岗位名称不能为空");
        }
        if (request.getSalaryMin() != null && request.getSalaryMax() != null
                && request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
            throw ApiException.badRequest("薪资范围无效");
        }
    }

    private static RecruitmentJobPo toPo(RecruitmentJobCreateDTO request) {
        RecruitmentJobPo result = new RecruitmentJobPo();
        result.setJobName(request.getJobName());
        result.setJobCode(request.getJobCode());
        result.setJobDescription(cleanHtml(request.getJobDescription()));
        result.setRecruitmentRequirements(request.getRecruitmentRequirements());
        result.setSalaryMin(request.getSalaryMin());
        result.setSalaryMax(request.getSalaryMax());
        result.setWorkLocation(request.getWorkLocation());
        return result;
    }

    private static String cleanHtml(String html) {
        return html == null ? null : Jsoup.clean(html, Safelist.basic());
    }

    private static RecruitmentJobVO toVo(RecruitmentJobPo source) {
        RecruitmentJobVO result = new RecruitmentJobVO();
        result.setJobUuid(source.getJobUuid());
        result.setId(source.getId());
        result.setJobName(source.getJobName());
        result.setJobCode(source.getJobCode());
        result.setJobDescription(source.getJobDescription());
        result.setRecruitmentRequirements(source.getRecruitmentRequirements());
        result.setSalaryMin(source.getSalaryMin());
        result.setSalaryMax(source.getSalaryMax());
        result.setWorkLocation(source.getWorkLocation());
        result.setStatus(source.getStatus());
        result.setIsDeleted(source.getIsDeleted());
        result.setCreatedAt(source.getCreatedAt());
        result.setUpdatedAt(source.getUpdatedAt());
        return result;
    }
}
