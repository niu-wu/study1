package com.example.study11.controller;

import com.example.study11.entity.dto.DeclineRequest;
import com.example.study11.entity.dto.RejectRequest;
import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.TalentCategory;
import com.example.study11.entity.vo.RecruitmentRejectionVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RejectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 淘汰、放弃入职和人才库接口。 */
@RestController
@RequestMapping("/api/rejections")
public class RejectionController {

    private final RejectionService rejectionService;

    public RejectionController(RejectionService rejectionService) {
        this.rejectionService = rejectionService;
    }

    @PostMapping("/reject")
    public ResponseEntity<RecruitmentRejectionVO> reject(@RequestBody @Valid RejectRequest request,
                                                         HttpServletRequest httpServletRequest) {
        RecruitmentRejectionVO result = rejectionService.reject(request.getRecordUuid(),
                request.getRejectionStage(), request.getRejectionReason(), request.getTalentCategory(),
                request.getRemark(), request.getResumeAttachmentId(), currentUserId(httpServletRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/decline")
    public ResponseEntity<RecruitmentRejectionVO> decline(@RequestBody @Valid DeclineRequest request,
                                                          HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(rejectionService.decline(request.getRecordUuid(),
                request.getTalentCategory(), request.getRemark(), currentUserId(httpServletRequest)));
    }

    @GetMapping("/talent-pool")
    public ResponseEntity<List<RecruitmentRejectionVO>> findTalentPool(
            @RequestParam(required = false) TalentCategory talentCategory,
            @RequestParam(required = false) RejectionReason rejectionReason,
            @RequestParam(required = false) RejectionStage rejectionStage) {
        return ResponseEntity.ok(rejectionService.findTalentPool(talentCategory, rejectionReason, rejectionStage));
    }

    private static Integer currentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            long candidate = number.longValue();
            return candidate > 0 && candidate <= Integer.MAX_VALUE ? (int) candidate : null;
        }
        if (value instanceof String text) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
