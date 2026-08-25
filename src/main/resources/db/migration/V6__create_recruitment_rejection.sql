CREATE TABLE IF NOT EXISTS `recruitment_rejection`
(
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '淘汰记录编号',
    `record_uuid`           CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `action`                VARCHAR(20) NOT NULL COMMENT '处理动作：REJECT/DECLINE',
    `rejection_stage`       VARCHAR(30) NOT NULL COMMENT '处理阶段',
    `rejection_reason`      VARCHAR(50) NOT NULL COMMENT '处理原因',
    `remark`                VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `resume_attachment_id`  BIGINT UNSIGNED DEFAULT NULL COMMENT '关联简历附件编号',
    `talent_category`       VARCHAR(30) NOT NULL COMMENT '人才库分类',
    `operator_user_id`      INT NOT NULL COMMENT '操作人用户编号',
    `rejection_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_recruitment_rejection_record_uuid` (`record_uuid`),
    KEY `idx_recruitment_rejection_action` (`action`),
    KEY `idx_recruitment_rejection_stage` (`rejection_stage`),
    KEY `idx_recruitment_rejection_reason` (`rejection_reason`),
    KEY `idx_recruitment_rejection_talent_category` (`talent_category`),
    KEY `idx_recruitment_rejection_operator_user_id` (`operator_user_id`),
    CONSTRAINT `fk_recruitment_rejection_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_recruitment_rejection_resume`
        FOREIGN KEY (`resume_attachment_id`) REFERENCES `candidate_resume` (`id`),
    CONSTRAINT `fk_recruitment_rejection_operator`
        FOREIGN KEY (`operator_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_recruitment_rejection_action` CHECK (`action` IN ('REJECT', 'DECLINE'))
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '招聘淘汰与人才库记录表';
