CREATE TABLE IF NOT EXISTS `onboarding_record`
(
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '入职记录编号',
    `record_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    `user_id`             INT NOT NULL COMMENT '关联的系统用户编号',
    `onboarding_date`     DATE NOT NULL COMMENT '入职日期',
    `onboarding_note`     VARCHAR(500) DEFAULT NULL COMMENT '入职备注',
    `processed_by_user_id` INT NOT NULL COMMENT '办理人用户编号',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_onboarding_record_uuid` (`record_uuid`),
    UNIQUE KEY `uk_onboarding_user_id` (`user_id`),
    KEY `idx_onboarding_processed_by_user_id` (`processed_by_user_id`),
    CONSTRAINT `fk_onboarding_recruitment`
        FOREIGN KEY (`record_uuid`) REFERENCES `recruitment_info` (`record_uuid`),
    CONSTRAINT `fk_onboarding_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_onboarding_processed_by_user`
        FOREIGN KEY (`processed_by_user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci
    COMMENT = '入职关联记录表';
