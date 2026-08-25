package com.example.study11.controller;

import com.example.study11.entity.dto.OfferNoticeDraftRequest;
import com.example.study11.entity.dto.OfferNoticeSendRequest;
import com.example.study11.entity.vo.OfferNoticeVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.OfferNoticeService;
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

/** 录用通知接口。 */
@RestController
@RequestMapping("/api/offers")
public class OfferNoticeController {

    private final OfferNoticeService offerNoticeService;

    public OfferNoticeController(OfferNoticeService offerNoticeService) {
        this.offerNoticeService = offerNoticeService;
    }

    @PostMapping("/draft")
    public ResponseEntity<OfferNoticeVO> createDraft(@RequestBody @Valid OfferNoticeDraftRequest request,
                                                     HttpServletRequest httpServletRequest) {
        OfferNoticeVO result = offerNoticeService.createDraft(request.getRecordUuid(),
                request.getRecipientEmail(), request.getNoticeContent(), currentUserId(httpServletRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/send")
    public ResponseEntity<OfferNoticeVO> send(@RequestBody @Valid OfferNoticeSendRequest request,
                                              HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(offerNoticeService.send(request.getRecordUuid(),
                currentUserId(httpServletRequest)));
    }

    @GetMapping("/{recordUuid}")
    public ResponseEntity<OfferNoticeVO> findByRecordUuid(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid) {
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(offerNoticeService.findByRecordUuid(recordUuid));
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
