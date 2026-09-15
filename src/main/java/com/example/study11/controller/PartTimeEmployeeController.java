package com.example.study11.controller;

import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.PartTimeEmployeeCreateVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.PartTimeEmployeeService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** 兼职人员登记接口，仅 HR/ADMIN。当前没有独立项目经理角色。 */
@RestController
@RequestMapping("/api/part-time-employees")
@Validated
public class PartTimeEmployeeController {

    private final PartTimeEmployeeService partTimeEmployeeService;

    public PartTimeEmployeeController(PartTimeEmployeeService partTimeEmployeeService) {
        this.partTimeEmployeeService = partTimeEmployeeService;
    }

    @PostMapping
    public ResponseEntity<PartTimeEmployeeCreateVO> create(
            @RequestBody @Valid EmployeeOnboardingSaveRequest requestBody,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partTimeEmployeeService.create(requestBody, currentUserId(request)));
    }

    @GetMapping("/{employeeUuid}")
    public ResponseEntity<EmployeeOnboardingFormVO> detail(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest request) {
        return ResponseEntity.ok(
                partTimeEmployeeService.findByEmployeeUuid(employeeUuid, currentUserId(request)));
    }

    @PostMapping(value = "/{employeeUuid}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeOnboardingFormVO> uploadPhoto(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        RecruitmentRequestValidator.validateNonEmptyFile(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partTimeEmployeeService.uploadPhoto(employeeUuid, file, currentUserId(request)));
    }

    @GetMapping("/{employeeUuid}/photo")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest request) {
        return toPhotoResponse(partTimeEmployeeService.loadPhoto(employeeUuid, currentUserId(request)));
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
