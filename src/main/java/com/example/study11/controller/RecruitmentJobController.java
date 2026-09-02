package com.example.study11.controller;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.RecruitmentJobCreateDTO;
import com.example.study11.entity.dto.RecruitmentJobPageRequest;
import com.example.study11.entity.dto.RecruitmentJobStatusTransitionDTO;
import com.example.study11.entity.dto.RecruitmentJobUpdateDTO;
import com.example.study11.entity.dto.RecruitmentAllocationStatusTransitionDTO;
import com.example.study11.entity.vo.RecruitmentJobVO;
import com.example.study11.exception.ApiException;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RecruitmentJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 招聘岗位后端接口。 */
@RestController
@RequestMapping("/api/recruitment-jobs")
@Validated
public class RecruitmentJobController {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

    private final RecruitmentJobService recruitmentJobService;

    public RecruitmentJobController(RecruitmentJobService recruitmentJobService) {
        this.recruitmentJobService = recruitmentJobService;
    }

    @PostMapping
    public ResponseEntity<RecruitmentJobVO> create(@RequestBody @Valid RecruitmentJobCreateDTO request,
                                                   HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recruitmentJobService.create(request, currentUserId(httpRequest)));
    }

    @GetMapping("/page")
    public ResponseEntity<PageResult<RecruitmentJobVO>> page(@Valid RecruitmentJobPageRequest request,
                                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(recruitmentJobService.findPage(request, currentUserId(httpRequest)));
    }

    @GetMapping("/{jobUuid}")
    public ResponseEntity<RecruitmentJobVO> detail(
            @PathVariable @Pattern(regexp = UUID_REGEX, message = "岗位 UUID 格式不正确") String jobUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(recruitmentJobService.findByJobUuid(jobUuid, currentUserId(httpRequest)));
    }

    @PutMapping("/{jobUuid}")
    public ResponseEntity<RecruitmentJobVO> update(
            @PathVariable @Pattern(regexp = UUID_REGEX, message = "岗位 UUID 格式不正确") String jobUuid,
            @RequestBody @Valid RecruitmentJobUpdateDTO request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(recruitmentJobService.update(jobUuid, request, currentUserId(httpRequest)));
    }

    @PatchMapping("/{jobUuid}/status")
    public ResponseEntity<RecruitmentJobVO> transitionStatus(
            @PathVariable @Pattern(regexp = UUID_REGEX, message = "岗位 UUID 格式不正确") String jobUuid,
            @RequestBody @Valid RecruitmentJobStatusTransitionDTO request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(recruitmentJobService.transitionStatus(jobUuid, request, currentUserId(httpRequest)));
    }

    @DeleteMapping("/{jobUuid}")
    public ResponseEntity<Void> delete(
            @PathVariable @Pattern(regexp = UUID_REGEX, message = "岗位 UUID 格式不正确") String jobUuid,
            HttpServletRequest httpRequest) {
        recruitmentJobService.delete(jobUuid, currentUserId(httpRequest));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{jobUuid}/companies/{allocationUuid}/status")
    public ResponseEntity<Void> transitionAllocationStatus(
            @PathVariable @Pattern(regexp = UUID_REGEX, message = "岗位 UUID 格式不正确") String jobUuid,
            @PathVariable @Pattern(regexp = UUID_REGEX, message = "配额 UUID 格式不正确") String allocationUuid,
            @RequestBody @Valid RecruitmentAllocationStatusTransitionDTO request,
            HttpServletRequest httpRequest) {
        recruitmentJobService.transitionAllocationStatus(jobUuid, allocationUuid, request, currentUserId(httpRequest));
        return ResponseEntity.ok().build();
    }

    private Integer currentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        if (value instanceof Integer userId && userId > 0) {
            return userId;
        }
        throw ApiException.unauthorized("未登录");
    }
}
