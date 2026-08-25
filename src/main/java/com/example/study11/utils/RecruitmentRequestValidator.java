package com.example.study11.utils;

import com.example.study11.exception.ApiException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.regex.Pattern;

/** 招聘接口路径参数和 multipart 参数的边界校验。 */
public final class RecruitmentRequestValidator {

    public static final String UUID_REGEX =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    private RecruitmentRequestValidator() {
    }

    /** 校验招聘记录业务 UUID，拒绝非标准 UUID 字符串。 */
    public static void validateRecordUuid(String recordUuid) {
        if (recordUuid == null || recordUuid.isBlank()) {
            throw ApiException.badRequest("招聘记录 UUID 不能为空");
        }
        if (!UUID_PATTERN.matcher(recordUuid).matches()) {
            throw ApiException.badRequest("招聘记录 UUID 格式不正确");
        }
        try {
            UUID.fromString(recordUuid);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("招聘记录 UUID 格式不正确");
        }
    }

    /** 校验数据库编号必须为正数。 */
    public static void validatePositiveId(Long id, String message) {
        if (id == null || id <= 0) {
            throw ApiException.badRequest(message);
        }
    }

    /** 校验简历 multipart 文件不能为空。 */
    public static void validateNonEmptyFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("上传文件不能为空");
        }
    }
}
