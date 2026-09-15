package com.example.study11.controller;

import com.example.study11.entity.vo.EmployeeAssignmentConfirmVO;
import com.example.study11.entity.vo.EmployeeAssignmentListItemVO;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.EmployeeAssignmentService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** 人员分配接口，仅 HR/ADMIN。 */
@RestController
@RequestMapping("/api/employee-assignments")
@Validated
public class EmployeeAssignmentController {

    private final EmployeeAssignmentService employeeAssignmentService;

    public EmployeeAssignmentController(EmployeeAssignmentService employeeAssignmentService) {
        this.employeeAssignmentService = employeeAssignmentService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeAssignmentListItemVO>> listPending(HttpServletRequest request) {
        return ResponseEntity.ok(employeeAssignmentService.listPending(currentUserId(request)));
    }

    @GetMapping("/{employeeUuid}")
    public ResponseEntity<EmployeeOnboardingFormVO> detail(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest request) {
        return ResponseEntity.ok(employeeAssignmentService.getDetail(employeeUuid, currentUserId(request)));
    }

    @GetMapping("/{employeeUuid}/photo")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest request) {
        return toPhotoResponse(employeeAssignmentService.loadPhoto(employeeUuid, currentUserId(request)));
    }

    @PostMapping("/{employeeUuid}/confirm")
    public ResponseEntity<EmployeeAssignmentConfirmVO> confirm(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest request) {
        return ResponseEntity.ok(employeeAssignmentService.confirm(employeeUuid, currentUserId(request)));
    }

    private static ResponseEntity<Resource> toPhotoResponse(EmployeePhotoFileVO photo) {
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(photo.getFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(parsePhotoContentType(photo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(photo.getResource());
    }

    private static MediaType parsePhotoContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return switch (contentType.trim().toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> MediaType.IMAGE_JPEG;
            case "image/png" -> MediaType.IMAGE_PNG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
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
