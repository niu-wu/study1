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
import com.example.study11.service.RecruitmentInfoService;
import com.example.study11.service.RecruitmentStatusService;
import jakarta.validation.Valid;
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

    /**
     * 保留单服务构造器，便于现有 Controller 切片测试继续覆盖基础 CRUD。
     */
    public RecruitmentInfoController(RecruitmentInfoService recruitmentInfoService) {
        this(recruitmentInfoService, null);
    }

    @Autowired
    public RecruitmentInfoController(RecruitmentInfoService recruitmentInfoService,
                                     RecruitmentStatusService recruitmentStatusService) {
        this.recruitmentInfoService = recruitmentInfoService;
        this.recruitmentStatusService = recruitmentStatusService;
    }

    @PostMapping
    public ResponseEntity<RecruitmentInfoVO> create(
            @RequestBody @Valid RecruitmentInfoCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recruitmentInfoService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RecruitmentInfoVO>> findList(
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(recruitmentInfoService.findList(applicantName, position, status));
    }

    @GetMapping("/page")
    public ResponseEntity<PageResult<RecruitmentInfoVO>> findPage(
            @Valid @ModelAttribute RecruitmentInfoPageRequest request) {
        return ResponseEntity.ok(recruitmentInfoService.findPage(request));
    }

    @GetMapping("/statistics")
    public ResponseEntity<RecruitmentInfoStatisticsVO> findStatistics() {
        return ResponseEntity.ok(recruitmentInfoService.findStatistics());
    }

    @GetMapping("/{recordUuid}")
    public ResponseEntity<RecruitmentInfoVO> findByRecordUuid(
            @PathVariable String recordUuid) {
        return ResponseEntity.ok(recruitmentInfoService.findByRecordUuid(recordUuid));
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<RecruitmentInfoVO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentInfoService.findById(id));
    }

    @PutMapping("/{recordUuid}")
    public ResponseEntity<RecruitmentInfoVO> update(
            @PathVariable String recordUuid,
            @RequestBody @Valid RecruitmentInfoUpdateDTO request) {
        return ResponseEntity.ok(recruitmentInfoService.update(recordUuid, request));
    }

    @PostMapping("/{recordUuid}/status")
    public ResponseEntity<RecruitmentInfoVO> transitionStatus(
            @PathVariable String recordUuid,
            @RequestBody @Valid RecruitmentStatusTransitionDTO request,
            HttpServletRequest httpServletRequest) {
        Integer operatorUserId = (Integer) httpServletRequest.getAttribute(
                TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        return ResponseEntity.ok(recruitmentStatusService.transition(recordUuid, request, operatorUserId));
    }

    @GetMapping("/{recordUuid}/status-history")
    public ResponseEntity<List<RecruitmentStatusHistoryVO>> findStatusHistory(
            @PathVariable String recordUuid) {
        return ResponseEntity.ok(recruitmentStatusService.findHistory(recordUuid));
    }

    @DeleteMapping("/{recordUuid}")
    public ResponseEntity<Void> delete(@PathVariable String recordUuid) {
        recruitmentInfoService.delete(recordUuid);
        return ResponseEntity.noContent().build();
    }
}
