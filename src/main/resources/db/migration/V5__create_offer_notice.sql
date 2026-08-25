CREATE TABLE IF NOT EXISTS `offer_notice`
(
    `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '录用通知编号',
    `record_uuid`        CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `recipient_email`    VARCHAR(255) DEFAULT NULL COMMENT '通知收件邮箱',
    `notice_content`     TEXT DEFAULT NULL COMMENT '录用通知草稿内容',
    `status`             VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '通知状态：DRAFT/SENT',
    `drafted_by_user_id` INT NOT NULL COMMENT '草稿创建人用户编号',
    `drafted_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '草稿时间',
    `sent_by_user_id`    INT DEFAULT NULL COMMENT '发送人用户编号',
    `sent_at`            DATETIME DEFAULT NULL COMMENT '模拟发送时间',
    `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_offer_notice_record_uuid` (`record_uuid`),
    KEY `idx_offer_notice_status` (`status`),
    KEY `idx_offer_notice_drafted_by_user_id` (`drafted_by_user_id`),
    KEY `idx_offer_notice_sent_by_user_id` (`sent_by_user_id`),
    CONSTRAINT `fk_offer_notice_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_offer_notice_drafted_by_user`
        FOREIGN KEY (`drafted_by_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_offer_notice_sent_by_user`
        FOREIGN KEY (`sent_by_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_offer_notice_status` CHECK (`status` IN ('DRAFT', 'SENT'))
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '录用通知表';
