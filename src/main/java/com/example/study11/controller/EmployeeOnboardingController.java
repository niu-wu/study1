package com.example.study11.controller;

import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.EmployeeOnboardingService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** 当前登录员工的入职登记表接口。 */
@RestController
@RequestMapping("/api/employee-onboarding")
public class EmployeeOnboardingController {

    private final EmployeeOnboardingService employeeOnboardingService;

    public EmployeeOnboardingController(EmployeeOnboardingService employeeOnboardingService) {
        this.employeeOnboardingService = employeeOnboardingService;
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeOnboardingFormVO> getMyForm(HttpServletRequest request) {
        return ResponseEntity.ok(employeeOnboardingService.getMyForm(currentUserId(request)));
    }

    @PutMapping("/me")
    public ResponseEntity<EmployeeOnboardingFormVO> saveDraft(
            @RequestBody @Valid EmployeeOnboardingSaveRequest requestBody,
            HttpServletRequest request) {
        return ResponseEntity.ok(employeeOnboardingService.saveDraft(currentUserId(request), requestBody));
    }

    @PostMapping("/me/submit")
    public ResponseEntity<EmployeeOnboardingFormVO> submit(
            @RequestBody @Valid EmployeeOnboardingSaveRequest requestBody,
            HttpServletRequest request) {
        return ResponseEntity.ok(employeeOnboardingService.submit(currentUserId(request), requestBody));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeOnboardingFormVO> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        RecruitmentRequestValidator.validateNonEmptyFile(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeOnboardingService.uploadPhoto(currentUserId(request), file));
    }

    @GetMapping("/me/photo")
    public ResponseEntity<Resource> downloadPhoto(HttpServletRequest request) {
        return toPhotoResponse(employeeOnboardingService.loadMyPhoto(currentUserId(request)));
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
