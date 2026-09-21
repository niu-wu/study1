-- V16: 劳动合同 + 合同/证件附件表
-- 劳动合同与续签合同合并一张表，用 sign_type 区分；附件存 OSS 地址不存文件本体。

CREATE TABLE IF NOT EXISTS `employee_contract`
(
    `contract_uuid`        CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '合同业务标识',
    `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自增编号',
    `employee_uuid`        CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `contract_no`          VARCHAR(50) DEFAULT NULL COMMENT '合同编号',
    `sign_type`           VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT '签订类型：NEW 新签，RENEWAL 续签',
    `contract_term_type`   VARCHAR(20) NOT NULL DEFAULT 'FIXED' COMMENT '合同期限：FIXED 有固定期限，UNLIMITED 无固定期限',
    `start_date`          DATE DEFAULT NULL COMMENT '合同起始日',
    `end_date`            DATE DEFAULT NULL COMMENT '合同到期日',
    `salary`              DECIMAL(12, 2) DEFAULT NULL COMMENT '合同薪资',
    `probation_months`    INT DEFAULT NULL COMMENT '试用期月数',
    `probation_salary`    DECIMAL(12, 2) DEFAULT NULL COMMENT '试用期薪资',
    `company_name`        VARCHAR(100) DEFAULT NULL COMMENT '所属公司/服务单位',
    `social_security_no` VARCHAR(50) DEFAULT NULL COMMENT '社保编号',
    `housing_fund_no`     VARCHAR(50) DEFAULT NULL COMMENT '公积金账号',
    `contract_status`     VARCHAR(20) NOT NULL DEFAULT 'EXECUTING' COMMENT '状态：EXECUTING 执行中，EXPIRED 已到期',
    `created_by`          INT DEFAULT NULL COMMENT '创建人',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`          INT DEFAULT NULL COMMENT '最后修改人',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`             TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删，1 已删',
    PRIMARY KEY (`contract_uuid`),
    UNIQUE KEY `uk_employee_contract_id` (`id`),
    KEY `idx_employee_contract_employee` (`employee_uuid`, `deleted`),
    CONSTRAINT `fk_employee_contract_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工劳动合同（含续签）';

CREATE TABLE IF NOT EXISTS `employee_contract_attachment`
(
    `attachment_uuid`   CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '附件业务标识',
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自增编号',
    `employee_uuid`     CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `attachment_type`   VARCHAR(30) NOT NULL COMMENT '附件类型：RESUME/ONBOARDING_CONTRACT/NDA/REGULARIZATION_FORM/SALARY_ADJUSTMENT_FORM/RENEWAL_CONTRACT/DIPLOMA/DEGREE_CERTIFICATE',
    `file_name`         VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url`          VARCHAR(500) NOT NULL COMMENT '文件 OSS 访问地址',
    `file_type`         VARCHAR(50) DEFAULT NULL COMMENT '文件 MIME 类型',
    `file_size`         BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `sort_order`        INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_by`        INT DEFAULT NULL COMMENT '创建人',
    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`        INT DEFAULT NULL COMMENT '最后修改人',
    `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`           TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删，1 已删',
    PRIMARY KEY (`attachment_uuid`),
    UNIQUE KEY `uk_employee_attachment_id` (`id`),
    KEY `idx_employee_attachment_employee` (`employee_uuid`, `attachment_type`, `deleted`),
    CONSTRAINT `fk_employee_attachment_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工合同/证件附件（存 OSS 地址）';
