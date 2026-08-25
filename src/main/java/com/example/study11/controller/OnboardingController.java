package com.example.study11.controller;

import com.example.study11.entity.dto.OnboardingRequest;
import com.example.study11.entity.vo.OnboardingRecordVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.OnboardingService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 入职办理接口。 */
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/process")
    public ResponseEntity<OnboardingRecordVO> process(@RequestBody @Valid OnboardingRequest request,
                                                      HttpServletRequest httpServletRequest) {
        OnboardingRecordVO result = onboardingService.process(request.getRecordUuid(),
                request.getOnboardingDate(), request.getOnboardingNote(), currentUserId(httpServletRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{recordUuid}")
    public ResponseEntity<OnboardingRecordVO> findByRecordUuid(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid) {
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(onboardingService.findByRecordUuid(recordUuid));
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
