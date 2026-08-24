CREATE TABLE IF NOT EXISTS `recruitment_status_history`
(
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '状态历史编号',
    `record_uuid`       CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `from_status`       VARCHAR(20) NOT NULL COMMENT '变更前状态',
    `to_status`         VARCHAR(20) NOT NULL COMMENT '变更后状态',
    `action`            VARCHAR(40) NOT NULL COMMENT '状态变更动作',
    `operator_user_id`  INT NOT NULL COMMENT '操作人用户编号',
    `remark`            VARCHAR(500) DEFAULT NULL COMMENT '状态变更备注',
    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_history_record_uuid` (`record_uuid`),
    KEY `idx_status_history_operator_user_id` (`operator_user_id`),
    CONSTRAINT `fk_status_history_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_status_history_operator`
        FOREIGN KEY (`operator_user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '招聘状态历史表';
