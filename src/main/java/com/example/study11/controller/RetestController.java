package com.example.study11.controller;

import com.example.study11.entity.dto.RetestApplicationRequest;
import com.example.study11.entity.dto.RetestReviewRequest;
import com.example.study11.entity.vo.RetestDetailsVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RetestService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 复试申请与安排确认接口。 */
@RestController
@RequestMapping("/api/retest")
public class RetestController {

    private final RetestService retestService;

    public RetestController(RetestService retestService) {
        this.retestService = retestService;
    }

    @PostMapping("/apply")
    public ResponseEntity<RetestDetailsVO> apply(
            @RequestBody @Valid RetestApplicationRequest request,
            HttpServletRequest httpServletRequest) {
        Integer applicantUserId = currentUserId(httpServletRequest);
        RetestDetailsVO result = retestService.apply(request.getRecordUuid(), request.getApplicantRemark(),
                applicantUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/confirm")
    public ResponseEntity<RetestDetailsVO> confirm(
            @RequestBody @Valid RetestReviewRequest request,
            HttpServletRequest httpServletRequest) {
        Integer reviewerUserId = currentUserId(httpServletRequest);
        RetestDetailsVO result = retestService.confirm(request.getRecordUuid(), request.getRetestCompany(),
                request.getRetestContactPerson(), request.getRetestTime(), reviewerUserId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{recordUuid}")
    public ResponseEntity<RetestDetailsVO> findByRecordUuid(
            @PathVariable String recordUuid) {
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(retestService.findByRecordUuid(recordUuid));
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
