package com.example.study11.controller;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.EmployeeArchivePageRequest;
import com.example.study11.entity.dto.EmployeeArchiveUpdateRequest;
import com.example.study11.entity.dto.EmployeeAssignmentSaveRequest;
import com.example.study11.entity.dto.EmployeeInterviewSaveRequest;
import com.example.study11.entity.dto.EmployeeSalarySaveRequest;
import com.example.study11.entity.dto.EmployeeSystemAccountSaveRequest;
import com.example.study11.entity.vo.EmployeeArchiveDetailVO;
import com.example.study11.entity.vo.EmployeeArchiveListItemVO;
import com.example.study11.entity.vo.EmployeeArchiveStatisticsVO;
import com.example.study11.entity.vo.EmployeeAssignmentListVO;
import com.example.study11.entity.vo.EmployeeAssignmentRecordVO;
import com.example.study11.entity.vo.EmployeeInterviewVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.EmployeePrintPreviewVO;
import com.example.study11.entity.vo.EmployeeSalaryListVO;
import com.example.study11.entity.vo.EmployeeSalaryRecordVO;
import com.example.study11.entity.vo.EmployeeSystemAccountVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.EmployeeArchiveService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** 员工档案列表、详情、统计、有限 PATCH 和月度薪资，仅 HR/ADMIN。 */
@RestController
@RequestMapping("/api/employee-archives")
@Validated
public class EmployeeArchiveController {

    private final EmployeeArchiveService employeeArchiveService;

    public EmployeeArchiveController(EmployeeArchiveService employeeArchiveService) {
        this.employeeArchiveService = employeeArchiveService;
    }

    @GetMapping("/page")
    public ResponseEntity<PageResult<EmployeeArchiveListItemVO>> findPage(
            @Valid @ModelAttribute EmployeeArchivePageRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.findPage(request, currentUserId(httpRequest)));
    }

    @GetMapping("/statistics")
    public ResponseEntity<EmployeeArchiveStatisticsVO> findStatistics(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.findStatistics(currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}")
    public ResponseEntity<EmployeeArchiveDetailVO> getDetail(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.getDetail(employeeUuid, currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}/photo")
    public ResponseEntity<Resource> downloadPhoto(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return toPhotoResponse(employeeArchiveService.loadPhoto(employeeUuid, currentUserId(httpRequest)));
    }

    @PatchMapping("/{employeeUuid}")
    public ResponseEntity<EmployeeArchiveListItemVO> update(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            @RequestBody @Valid EmployeeArchiveUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.update(employeeUuid, request, currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}/salaries")
    public ResponseEntity<EmployeeSalaryListVO> listSalaries(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.listSalaries(employeeUuid, currentUserId(httpRequest)));
    }

    @PostMapping("/{employeeUuid}/salaries")
    public ResponseEntity<EmployeeSalaryRecordVO> saveSalary(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            @RequestBody @Valid EmployeeSalarySaveRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeArchiveService.saveSalary(employeeUuid, request, currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}/assignments")
    public ResponseEntity<EmployeeAssignmentListVO> listAssignments(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.listAssignments(employeeUuid, currentUserId(httpRequest)));
    }

    @PostMapping("/{employeeUuid}/assignments")
    public ResponseEntity<EmployeeAssignmentRecordVO> saveAssignment(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            @RequestBody @Valid EmployeeAssignmentSaveRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeArchiveService.saveAssignment(employeeUuid, request, currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}/accounts")
    public ResponseEntity<List<EmployeeSystemAccountVO>> listAccounts(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.listAccounts(employeeUuid, currentUserId(httpRequest)));
    }

    @PostMapping("/{employeeUuid}/accounts")
    public ResponseEntity<EmployeeSystemAccountVO> saveAccount(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            @RequestBody @Valid EmployeeSystemAccountSaveRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeArchiveService.saveAccount(employeeUuid, request, currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}/interviews")
    public ResponseEntity<List<EmployeeInterviewVO>> listInterviews(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.listInterviews(employeeUuid, currentUserId(httpRequest)));
    }

    @PostMapping("/{employeeUuid}/interviews")
    public ResponseEntity<EmployeeInterviewVO> saveInterview(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            @RequestBody @Valid EmployeeInterviewSaveRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeArchiveService.saveInterview(employeeUuid, request, currentUserId(httpRequest)));
    }

    @GetMapping("/{employeeUuid}/print-preview")
    public ResponseEntity<EmployeePrintPreviewVO> printPreview(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "员工档案 UUID 格式不正确") String employeeUuid,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(employeeArchiveService.getPrintPreview(employeeUuid, currentUserId(httpRequest)));
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
