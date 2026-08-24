CREATE TABLE IF NOT EXISTS `candidate_resume`
(
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '简历附件编号',
    `record_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `original_filename`   VARCHAR(255) NOT NULL COMMENT '上传时的原始文件名',
    `stored_filename`     VARCHAR(64) CHARACTER SET ascii NOT NULL COMMENT '随机生成的安全存储文件名',
    `file_size`           BIGINT UNSIGNED NOT NULL COMMENT '文件大小，单位为字节',
    `content_type`        VARCHAR(100) NOT NULL COMMENT '文件内容类型',
    `uploader_user_id`   INT NOT NULL COMMENT '上传人用户编号',
    `uploaded_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_candidate_resume_stored_filename` (`stored_filename`),
    KEY `idx_candidate_resume_record_uuid` (`record_uuid`),
    KEY `idx_candidate_resume_uploader_user_id` (`uploader_user_id`),
    CONSTRAINT `chk_candidate_resume_file_size` CHECK (`file_size` > 0),
    CONSTRAINT `fk_candidate_resume_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_candidate_resume_uploader`
        FOREIGN KEY (`uploader_user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '候选人简历附件表';
