package com.example.study11.controller;

import com.example.study11.entity.vo.CandidateResumeVO;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.ResumeStorageService;
import com.example.study11.utils.RecruitmentRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.List;

/** 候选人简历附件接口。 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeStorageService resumeStorageService;

    public ResumeController(ResumeStorageService resumeStorageService) {
        this.resumeStorageService = resumeStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateResumeVO> upload(
            @RequestParam("recordUuid") String recordUuid,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        RecruitmentRequestValidator.validateNonEmptyFile(file);
        Integer currentUserId = currentUserId(request);
        CandidateResumeVO result = resumeStorageService.storeFile(recordUuid, file, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResumeVO> findById(
            @PathVariable @Positive(message = "简历附件编号必须为正数") Long id) {
        RecruitmentRequestValidator.validatePositiveId(id, "简历附件编号必须为正数");
        return ResponseEntity.ok(resumeStorageService.findById(id));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable @Positive(message = "简历附件编号必须为正数") Long id) {
        RecruitmentRequestValidator.validatePositiveId(id, "简历附件编号必须为正数");
        CandidateResumeVO metadata = resumeStorageService.findById(id);
        Resource resource = resumeStorageService.loadFile(id);
        MediaType contentType = parseContentType(metadata.getContentType());
        String filename = safeDownloadFilename(metadata.getOriginalFilename(), id, contentType);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @GetMapping("/list/{recordUuid}")
    public ResponseEntity<List<CandidateResumeVO>> findByRecordUuid(
            @PathVariable @Pattern(regexp = RecruitmentRequestValidator.UUID_REGEX,
                    message = "招聘记录 UUID 格式不正确") String recordUuid) {
        RecruitmentRequestValidator.validateRecordUuid(recordUuid);
        return ResponseEntity.ok(resumeStorageService.findByRecordUuid(recordUuid));
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

    private static MediaType parseContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return switch (contentType.trim().toLowerCase(Locale.ROOT)) {
            case "application/pdf" -> MediaType.APPLICATION_PDF;
            case "application/msword" -> MediaType.parseMediaType("application/msword");
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private static String safeDownloadFilename(String originalFilename, Long id, MediaType contentType) {
        String expectedExtension = extensionFor(contentType);
        if (!".bin".equals(expectedExtension)
                && isSafeFilename(originalFilename)
                && expectedExtension.equals(extensionOf(originalFilename))) {
            return originalFilename;
        }
        return "resume-" + (id == null ? "file" : id) + expectedExtension;
    }

    private static boolean isSafeFilename(String filename) {
        if (filename == null || filename.isBlank() || !filename.equals(filename.strip())) {
            return false;
        }
        if (filename.equals(".") || filename.equals("..")
                || filename.indexOf('/') >= 0 || filename.indexOf('\\') >= 0
                || filename.indexOf(':') >= 0 || filename.length() > 255) {
            return false;
        }
        for (int index = 0; index < filename.length(); index++) {
            if (Character.isISOControl(filename.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private static String extensionOf(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex <= 0 || extensionIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(extensionIndex).toLowerCase(Locale.ROOT);
    }

    private static String extensionFor(MediaType contentType) {
        if (MediaType.APPLICATION_PDF.equals(contentType)) {
            return ".pdf";
        }
        if ("application/msword".equals(contentType.toString())) {
            return ".doc";
        }
        if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                .equals(contentType.toString())) {
            return ".docx";
        }
        return ".bin";
    }
}
