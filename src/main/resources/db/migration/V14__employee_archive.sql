-- Employee archive: list/header fields, monthly salary snapshots, assignment events, system accounts.
-- Attendance request flows stay out of this version. Day counts on salary rows are manual nullable snapshots.

ALTER TABLE `employee`
    ADD COLUMN `department` VARCHAR(100) DEFAULT NULL
        COMMENT '部门' AFTER `employment_type`,
    ADD COLUMN `company_email` VARCHAR(100) DEFAULT NULL
        COMMENT '企业邮箱' AFTER `department`,
    ADD COLUMN `employment_status` VARCHAR(20) NOT NULL DEFAULT 'PROBATION'
        COMMENT '在职状态：PROBATION、REGULAR、RESIGNED' AFTER `company_email`,
    ADD COLUMN `work_location` VARCHAR(20) NOT NULL DEFAULT 'HEADQUARTERS'
        COMMENT '工作地点：HEADQUARTERS、DISPATCHED' AFTER `employment_status`,
    ADD COLUMN `hired_at` DATE DEFAULT NULL
        COMMENT '入职时间' AFTER `work_location`,
    ADD COLUMN `probation_end_date` DATE DEFAULT NULL
        COMMENT '预计转正日' AFTER `hired_at`,
    ADD COLUMN `regularized_at` DATE DEFAULT NULL
        COMMENT '实际转正日' AFTER `probation_end_date`,
    ADD COLUMN `customer_name` VARCHAR(100) DEFAULT NULL
        COMMENT '当前客户，由稼动事件维护' AFTER `regularized_at`,
    ADD COLUMN `contract_salary` DECIMAL(12, 2) DEFAULT NULL
        COMMENT '合同薪资，档案维护值' AFTER `customer_name`,
    ADD COLUMN `probation_salary` DECIMAL(12, 2) DEFAULT NULL
        COMMENT '试用期薪资，档案维护值' AFTER `contract_salary`,
    ADD KEY `idx_employee_status_location` (`employment_status`, `work_location`),
    ADD KEY `idx_employee_full_name` (`full_name`),
    ADD KEY `idx_employee_phone` (`phone`),
    ADD KEY `idx_employee_customer_name` (`customer_name`),
    ADD KEY `idx_employee_position` (`position`);

UPDATE `employee`
SET `employment_status` = 'REGULAR'
WHERE `employment_type` = 'PART_TIME';

UPDATE `employee` `e`
    LEFT JOIN `onboarding_record` `o` ON `e`.`record_uuid` = `o`.`record_uuid`
SET `e`.`hired_at` = CASE
                         WHEN `e`.`employment_type` = 'FULL_TIME'
                             AND `o`.`onboarding_date` IS NOT NULL THEN `o`.`onboarding_date`
                         ELSE DATE(e.created_at)
    END
WHERE `e`.`hired_at` IS NULL;

ALTER TABLE `employee_training`
    ADD COLUMN `training_result` VARCHAR(200) DEFAULT NULL
        COMMENT '培训结果' AFTER `course_content`;

CREATE TABLE IF NOT EXISTS `employee_salary_record`
(
    `salary_uuid`         CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '月度薪资业务标识',
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `employee_uuid`       CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `salary_month`        DATE NOT NULL COMMENT '工资所属月，存该月1号',
    `department`          VARCHAR(100) DEFAULT NULL COMMENT '当时部门快照',
    `scheduled_days`      INT DEFAULT NULL COMMENT '应出勤天数，HR手工快照，不来源于考勤',
    `actual_days`         INT DEFAULT NULL COMMENT '实出勤天数，HR手工快照，不来源于考勤',
    `leave_days`          INT DEFAULT NULL COMMENT '请假天数，HR手工快照，不来源于考勤',
    `base_salary`         DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '基本工资',
    `position_allowance`  DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '岗位津贴',
    `overtime_pay`        DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '加班费金额，不是加班申请流水',
    `bonus`               DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '奖金',
    `subsidy`             DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '补贴',
    `other_pay`           DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '其他应发',
    `social_insurance`    DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '社保',
    `housing_fund`       DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '公积金',
    `tax_amount`          DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '个税',
    `tax_rate`            DECIMAL(5, 2) DEFAULT NULL COMMENT '展示用税率',
    `gross_pay`           DECIMAL(12, 2) NOT NULL COMMENT '应发，服务端计算后落库',
    `net_pay`             DECIMAL(12, 2) NOT NULL COMMENT '实发，服务端计算后落库',
    `remark`              VARCHAR(100) DEFAULT NULL COMMENT '试用期、转正、工龄一年调薪等',
    `created_by`          INT NOT NULL COMMENT '经办人',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`salary_uuid`),
    UNIQUE KEY `uk_employee_salary_record_id` (`id`),
    UNIQUE KEY `uk_employee_salary_month` (`employee_uuid`, `salary_month`),
    KEY `idx_employee_salary_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_salary_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_salary_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工月度薪资快照';

CREATE TABLE IF NOT EXISTS `employee_assignment_record`
(
    `assignment_uuid`     CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '稼动事件业务标识',
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `employee_uuid`       CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `assignment_type`     VARCHAR(20) NOT NULL COMMENT 'ENTER 进入客户，RETURN 回公司',
    `event_date`          DATE NOT NULL COMMENT '事件发生日',
    `company_name`        VARCHAR(100) DEFAULT NULL COMMENT '客户名称，ENTER 必填，RETURN 必须为空',
    `utilization_rate`    DECIMAL(5, 2) DEFAULT NULL COMMENT '稼动率，仅 ENTER 使用',
    `operator_user_id`    INT NOT NULL COMMENT '经办人',
    `remark`              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`assignment_uuid`),
    UNIQUE KEY `uk_employee_assignment_record_id` (`id`),
    KEY `idx_employee_assignment_event` (`employee_uuid`, `event_date`, `id`),
    CONSTRAINT `fk_employee_assignment_record_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_assignment_record_operator`
        FOREIGN KEY (`operator_user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工岗位稼动事件';

CREATE TABLE IF NOT EXISTS `employee_system_account`
(
    `account_uuid`        CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '系统账号业务标识',
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库自动编号，非主键',
    `employee_uuid`       CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '员工档案业务标识',
    `system_name`         VARCHAR(100) NOT NULL COMMENT '系统名称',
    `account_name`        VARCHAR(100) NOT NULL COMMENT '登录名或邮箱',
    `opened_at`           DATETIME DEFAULT NULL COMMENT '开通时间',
    `operator_user_id`    INT NOT NULL COMMENT '经办人',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`account_uuid`),
    UNIQUE KEY `uk_employee_system_account_id` (`id`),
    KEY `idx_employee_system_account_employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_system_account_employee`
        FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_system_account_operator`
        FOREIGN KEY (`operator_user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工岗位系统账号，不存密码';
