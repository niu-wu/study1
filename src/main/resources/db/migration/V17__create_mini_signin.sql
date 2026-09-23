-- V17: 应聘者小程序面试签到
-- 主表 + 教育经历子表 + 工作经历子表，表名前缀 mini_。

CREATE TABLE IF NOT EXISTS `mini_signin`
(
    `signin_uuid`      CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '签到业务标识',
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自增编号',
    `position_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT '应聘职位ID，关联 recruitment_job.id',
    `position_name`    VARCHAR(100) DEFAULT NULL COMMENT '应聘职位名称快照',
    `name`             VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender`           VARCHAR(10) DEFAULT NULL COMMENT '性别：MAN / WOMAN',
    `phone`            VARCHAR(20) NOT NULL COMMENT '手机号',
    `email`            VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `id_card`          VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `birth_date`       DATE DEFAULT NULL COMMENT '出生日期',
    `address`          VARCHAR(200) DEFAULT NULL COMMENT '现住地址',
    `apply_channel`    VARCHAR(20) NOT NULL COMMENT '应聘渠道：REFERRAL内推 / BOSS / ZHILIAN智联 / OTHER其他',
    `referrer`         VARCHAR(50) DEFAULT NULL COMMENT '应聘对接人',
    `interview_mode`   VARCHAR(20) NOT NULL COMMENT '面试方式：OFFLINE线下面试 / VIDEO视频面试',
    `created_by`       INT DEFAULT NULL COMMENT '创建人，应聘者未登录可为空',
    `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`       INT DEFAULT NULL COMMENT '最后修改人',
    `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`          TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`signin_uuid`),
    UNIQUE KEY `uk_mini_signin_id` (`id`),
    KEY `idx_mini_signin_phone` (`phone`),
    KEY `idx_mini_signin_position` (`position_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应聘者面试签到';

CREATE TABLE IF NOT EXISTS `mini_signin_education`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增编号',
    `signin_uuid` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '签到业务标识',
    `start_date`  VARCHAR(10) DEFAULT NULL COMMENT '教育开始月份，yyyy-MM',
    `end_date`    VARCHAR(10) DEFAULT NULL COMMENT '教育结束月份，yyyy-MM',
    `school_name` VARCHAR(100) DEFAULT NULL COMMENT '院校名称',
    `education`    VARCHAR(50) DEFAULT NULL COMMENT '学历',
    `major`       VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `sort_order`  INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_by`  INT DEFAULT NULL COMMENT '创建人',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`  INT DEFAULT NULL COMMENT '最后修改人',
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    KEY `idx_mini_signin_edu_signin` (`signin_uuid`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '签到教育经历';

CREATE TABLE IF NOT EXISTS `mini_signin_work`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增编号',
    `signin_uuid`   CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '签到业务标识',
    `start_date`    VARCHAR(10) DEFAULT NULL COMMENT '工作开始月份，yyyy-MM',
    `end_date`      VARCHAR(10) DEFAULT NULL COMMENT '工作结束月份，yyyy-MM',
    `company_name`  VARCHAR(100) DEFAULT NULL COMMENT '工作单位',
    `position`      VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `leave_reason`  VARCHAR(200) DEFAULT NULL COMMENT '离职原因',
    `sort_order`    INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_by`    INT DEFAULT NULL COMMENT '创建人',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`    INT DEFAULT NULL COMMENT '最后修改人',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`       TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    PRIMARY KEY (`id`),
    KEY `idx_mini_signin_work_signin` (`signin_uuid`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '签到工作经历';
