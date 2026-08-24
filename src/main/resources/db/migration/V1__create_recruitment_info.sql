CREATE TABLE IF NOT EXISTS `recruitment_info`
(
    `record_uuid`            CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `id`                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `applicant_name`         VARCHAR(50) NOT NULL COMMENT '应聘人姓名',
    `gender`                 VARCHAR(10) DEFAULT NULL COMMENT '性别',
    `position`               VARCHAR(100) NOT NULL COMMENT '岗位',
    `phone`                  VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email`                  VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    `application_channel`    VARCHAR(50) DEFAULT NULL COMMENT '应聘渠道',
    `application_method`     VARCHAR(50) DEFAULT NULL COMMENT '应聘方式',
    `status`                 VARCHAR(20) NOT NULL DEFAULT 'PENDING_INITIAL' COMMENT '招聘状态',
    `initial_contact_person` VARCHAR(50) DEFAULT NULL COMMENT '初试对接人',
    `initial_interview_time` DATETIME DEFAULT NULL COMMENT '初试时间',
    `retest_contact_person`  VARCHAR(50) DEFAULT NULL COMMENT '复试对接人',
    `retest_interview_time`  DATETIME DEFAULT NULL COMMENT '复试时间',
    `created_at`             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`record_uuid`),
    UNIQUE KEY `uk_recruitment_info_id` (`id`),
    KEY `idx_recruitment_info_phone` (`phone`),
    KEY `idx_recruitment_info_email` (`email`),
    KEY `idx_recruitment_info_status` (`status`),
    KEY `idx_recruitment_info_position` (`position`),
    KEY `idx_recruitment_info_created_at` (`created_at`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '招聘信息表';
