-- Recruitment company, job, company allocation and audit tables.
-- Business UUIDs are primary keys; numeric ids are database-generated unique numbers only.

CREATE TABLE IF NOT EXISTS `recruitment_company`
(
    `company_uuid`          CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '公司业务标识',
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `company_name`          VARCHAR(100) NOT NULL COMMENT '公司名称',
    `current_headcount`     INT NOT NULL DEFAULT 0 COMMENT '当前人数',
    `status`                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '公司状态',
    `is_deleted`            TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
    `created_by_user_id`    INT NOT NULL COMMENT '创建人用户编号',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`company_uuid`),
    UNIQUE KEY `uk_recruitment_company_id` (`id`),
    UNIQUE KEY `uk_recruitment_company_name` (`company_name`),
    KEY `idx_recruitment_company_status` (`status`),
    KEY `idx_recruitment_company_creator` (`created_by_user_id`),
    CONSTRAINT `fk_recruitment_company_creator`
        FOREIGN KEY (`created_by_user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '招聘公司表';

CREATE TABLE IF NOT EXISTS `recruitment_job`
(
    `job_uuid`              CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '岗位业务标识',
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `job_name`              VARCHAR(100) NOT NULL COMMENT '岗位名称',
    `job_code`              VARCHAR(50) DEFAULT NULL COMMENT '岗位编码',
    `job_description`       MEDIUMTEXT COMMENT '岗位职责清洗后的HTML',
    `recruitment_requirements` MEDIUMTEXT COMMENT '招聘要求',
    `salary_min`            DECIMAL(12,2) DEFAULT NULL COMMENT '最低薪资',
    `salary_max`            DECIMAL(12,2) DEFAULT NULL COMMENT '最高薪资',
    `work_location`         VARCHAR(100) DEFAULT NULL COMMENT '工作地点',
    `status`                VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '岗位状态：OPEN、CLOSED、COMPLETED',
    `is_deleted`            TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1已删除',
    `created_by_user_id`    INT NOT NULL COMMENT '创建人用户编号',
    `updated_by_user_id`    INT DEFAULT NULL COMMENT '最后修改人用户编号',
    `version`               INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`job_uuid`),
    UNIQUE KEY `uk_recruitment_job_id` (`id`),
    UNIQUE KEY `uk_recruitment_job_code` (`job_code`),
    KEY `idx_recruitment_job_status_deleted` (`status`, `is_deleted`),
    KEY `idx_recruitment_job_creator` (`created_by_user_id`),
    CONSTRAINT `fk_recruitment_job_creator`
        FOREIGN KEY (`created_by_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_recruitment_job_updater`
        FOREIGN KEY (`updated_by_user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '招聘岗位表';

CREATE TABLE IF NOT EXISTS `recruitment_job_company`
(
    `allocation_uuid`      CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '岗位公司配额业务标识',
    `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `job_uuid`             CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '岗位业务标识',
    `company_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '公司业务标识',
    `required_headcount`   INT NOT NULL COMMENT '该公司岗位需求人数',
    `status`               VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '公司配额状态：OPEN、CLOSED、COMPLETED',
    `closed_reason`        VARCHAR(500) DEFAULT NULL COMMENT '配额关闭或完成原因',
    `created_by_user_id`   INT NOT NULL COMMENT '创建人用户编号',
    `updated_by_user_id`   INT DEFAULT NULL COMMENT '最后修改人用户编号',
    `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`allocation_uuid`),
    UNIQUE KEY `uk_recruitment_job_company_id` (`id`),
    UNIQUE KEY `uk_job_company_allocation` (`job_uuid`, `company_uuid`),
    KEY `idx_job_company_company_uuid` (`company_uuid`),
    KEY `idx_job_company_status` (`status`),
    CONSTRAINT `fk_job_company_job`
        FOREIGN KEY (`job_uuid`) REFERENCES `recruitment_job` (`job_uuid`),
    CONSTRAINT `fk_job_company_company`
        FOREIGN KEY (`company_uuid`) REFERENCES `recruitment_company` (`company_uuid`),
    CONSTRAINT `fk_job_company_creator`
        FOREIGN KEY (`created_by_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_job_company_updater`
        FOREIGN KEY (`updated_by_user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位公司配额表';

CREATE TABLE IF NOT EXISTS `recruitment_job_status_history`
(
    `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '岗位状态历史编号',
    `job_uuid`             CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '岗位业务标识',
    `allocation_uuid`      CHAR(36) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL COMMENT '公司配额业务标识',
    `scope`                VARCHAR(20) NOT NULL COMMENT '状态范围：JOB、ALLOCATION',
    `from_status`          VARCHAR(20) DEFAULT NULL COMMENT '变更前状态',
    `to_status`            VARCHAR(20) NOT NULL COMMENT '变更后状态',
    `action`               VARCHAR(40) NOT NULL COMMENT '状态变更动作',
    `operator_user_id`     INT NOT NULL COMMENT '操作人用户编号',
    `remark`               VARCHAR(500) DEFAULT NULL COMMENT '变更备注',
    `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_status_history_job` (`job_uuid`),
    KEY `idx_job_status_history_allocation` (`allocation_uuid`),
    KEY `idx_job_status_history_operator` (`operator_user_id`),
    CONSTRAINT `fk_job_status_history_job`
        FOREIGN KEY (`job_uuid`) REFERENCES `recruitment_job` (`job_uuid`),
    CONSTRAINT `fk_job_status_history_allocation`
        FOREIGN KEY (`allocation_uuid`) REFERENCES `recruitment_job_company` (`allocation_uuid`),
    CONSTRAINT `fk_job_status_history_operator`
        FOREIGN KEY (`operator_user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位与公司配额状态审计表';
