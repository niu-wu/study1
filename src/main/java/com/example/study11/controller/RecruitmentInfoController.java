package com.example.study11.controller;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.RecruitmentInfoCreateDTO;
import com.example.study11.entity.dto.RecruitmentInfoPageRequest;
import com.example.study11.entity.dto.RecruitmentInfoUpdateDTO;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.vo.RecruitmentInfoStatisticsVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentStatusHistoryVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.service.RoleAuthorizationService;
import com.example.study11.service.RecruitmentInfoService;
import com.example.study11.service.RecruitmentStatusService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/** 招聘信息基础 CRUD 接口。 */
@RestController
@RequestMapping("/api/recruitment-info")
public class RecruitmentInfoController {

    private final RecruitmentInfoService recruitmentInfoService;

    private final RecruitmentStatusService recruitmentStatusService;

    private final RoleAuthorizationService roleAuthorizationService;

    /**
     * 保留单服务构造器，便于现有 Controller 切片测试继续覆盖基础 CRUD。
     */
    public RecruitmentInfoController(RecruitmentInfoService recruitmentInfoService) {
        this(recruitmentInfoService, null, null);
    }

    public RecruitmentInfoController(RecruitmentInfoService recruitmentInfoService,
                                     RecruitmentStatusService recruitmentStatusService) {
        this(recruitmentInfoService, recruitmentStatusService, null);
    }

    @Autowired
    public RecruitmentInfoController(RecruitmentInfoService recruitmentInfoService,
                                     RecruitmentStatusService recruitmentStatusService,
                                     RoleAuthorizationService roleAuthorizationService) {
        this.recruitmentInfoService = recruitmentInfoService;
        this.recruitmentStatusService = recruitmentStatusService;
        this.roleAuthorizationService = roleAuthorizationService;
    }

    @PostMapping
    public ResponseEntity<RecruitmentInfoVO> create(
            @RequestBody @Valid RecruitmentInfoCreateDTO request, HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recruitmentInfoService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RecruitmentInfoVO>> findList(
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        return ResponseEntity.ok(recruitmentInfoService.findList(applicantName, position, status));
    }

    @GetMapping("/page")
    public ResponseEntity<PageResult<RecruitmentInfoVO>> findPage(
            @Valid @ModelAttribute RecruitmentInfoPageRequest request, HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        return ResponseEntity.ok(recruitmentInfoService.findPage(request));
    }

    @GetMapping("/statistics")
    public ResponseEntity<RecruitmentInfoStatisticsVO> findStatistics(HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        return ResponseEntity.ok(recruitmentInfoService.findStatistics());
    }

    @GetMapping("/{recordUuid}")
    public ResponseEntity<RecruitmentInfoVO> findByRecordUuid(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid, HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(recruitmentInfoService.findByRecordUuid(recordUuid));
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<RecruitmentInfoVO> findById(@PathVariable @Positive(message = "招聘记录编号必须为正数") Long id,
                                                      HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        RecruitmentRequestValidator.validatePositiveId(id, "招聘记录编号必须为正数");
        return ResponseEntity.ok(recruitmentInfoService.findById(id));
    }

    @PutMapping("/{recordUuid}")
    public ResponseEntity<RecruitmentInfoVO> update(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid,
            @RequestBody @Valid RecruitmentInfoUpdateDTO request,
            HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(recruitmentInfoService.update(recordUuid, request));
    }

    @PostMapping("/{recordUuid}/status")
    public ResponseEntity<RecruitmentInfoVO> transitionStatus(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid,
            @RequestBody @Valid RecruitmentStatusTransitionDTO request,
            HttpServletRequest httpServletRequest) {
        requireHrOrAdmin(httpServletRequest);
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        Integer operatorUserId = (Integer) httpServletRequest.getAttribute(
                TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        return ResponseEntity.ok(recruitmentStatusService.transition(recordUuid, request, operatorUserId));
    }

    @GetMapping("/{recordUuid}/status-history")
    public ResponseEntity<List<RecruitmentStatusHistoryVO>> findStatusHistory(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid) {
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(recruitmentStatusService.findHistory(recordUuid));
    }

    @DeleteMapping("/{recordUuid}")
    public ResponseEntity<Void> delete(@PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
            message = "招聘记录 UUID 格式不正确") String recordUuid, HttpServletRequest httpRequest) {
        requireHrOrAdmin(httpRequest);
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        recruitmentInfoService.delete(recordUuid);
        return ResponseEntity.noContent().build();
    }

    private void requireHrOrAdmin(HttpServletRequest request) {
        if (roleAuthorizationService == null) {
            return;
        }
        Object value = request == null ? null : request.getAttribute(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        if (!(value instanceof Integer userId && userId > 0)) {
            throw com.example.study11.exception.ApiException.unauthorized("未登录");
        }
        roleAuthorizationService.requireAnyRole(userId, UserRole.HR, UserRole.ADMIN);
    }
}
