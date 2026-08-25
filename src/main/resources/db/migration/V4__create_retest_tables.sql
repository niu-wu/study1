SET @recruitment_type_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'recruitment_info'
      AND column_name = 'recruitment_type'
);
SET @add_recruitment_type_sql := IF(
    @recruitment_type_column_exists = 0,
    'ALTER TABLE `recruitment_info` ADD COLUMN `recruitment_type` VARCHAR(20) NOT NULL DEFAULT ''INTERNAL'' COMMENT ''招聘类型：INTERNAL/OUTSOURCED'' AFTER `status`',
    'SELECT 1'
);
PREPARE add_recruitment_type FROM @add_recruitment_type_sql;
EXECUTE add_recruitment_type;
DEALLOCATE PREPARE add_recruitment_type;

CREATE TABLE IF NOT EXISTS `retest_application`
(
    `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '复试申请编号',
    `record_uuid`        CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `application_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `applicant_remark`   VARCHAR(500) DEFAULT NULL COMMENT '申请备注',
    `applicant_user_id`  INT NOT NULL COMMENT '申请人用户编号',
    `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_retest_application_record_uuid` (`record_uuid`),
    KEY `idx_retest_application_applicant_user_id` (`applicant_user_id`),
    CONSTRAINT `fk_retest_application_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_retest_application_applicant`
        FOREIGN KEY (`applicant_user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '复试申请表';

SET @retest_application_unique_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'retest_application'
      AND index_name = 'uk_retest_application_record_uuid'
);
SET @add_retest_application_unique_sql := IF(
    @retest_application_unique_exists = 0,
    'ALTER TABLE `retest_application` ADD UNIQUE KEY `uk_retest_application_record_uuid` (`record_uuid`)',
    'SELECT 1'
);
PREPARE add_retest_application_unique FROM @add_retest_application_unique_sql;
EXECUTE add_retest_application_unique;
DEALLOCATE PREPARE add_retest_application_unique;

CREATE TABLE IF NOT EXISTS `retest_review`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '复试审核编号',
    `record_uuid`           CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `retest_company`        VARCHAR(255) DEFAULT NULL COMMENT '复试公司，外派招聘必填',
    `retest_contact_person` VARCHAR(50) DEFAULT NULL COMMENT '复试对接人，外派招聘必填',
    `retest_time`           DATETIME DEFAULT NULL COMMENT '复试时间，外派招聘必填',
    `reviewer_user_id`      INT NOT NULL COMMENT '审核人用户编号',
    `review_time`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_retest_review_record_uuid` (`record_uuid`),
    KEY `idx_retest_review_reviewer_user_id` (`reviewer_user_id`),
    CONSTRAINT `fk_retest_review_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_retest_review_reviewer`
        FOREIGN KEY (`reviewer_user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '复试安排审核表';

SET @retest_review_unique_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'retest_review'
      AND index_name = 'uk_retest_review_record_uuid'
);
SET @add_retest_review_unique_sql := IF(
    @retest_review_unique_exists = 0,
    'ALTER TABLE `retest_review` ADD UNIQUE KEY `uk_retest_review_record_uuid` (`record_uuid`)',
    'SELECT 1'
);
PREPARE add_retest_review_unique FROM @add_retest_review_unique_sql;
EXECUTE add_retest_review_unique;
DEALLOCATE PREPARE add_retest_review_unique;
