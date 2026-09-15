-- Employee assignment and part-time support.
-- Keep WeChat on employee; do not create contract, salary, or attachment tables.

ALTER TABLE `employee`
    MODIFY `record_uuid` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL
        COMMENT '入职办理对应的招聘记录，兼职可空',
    ADD COLUMN `employment_type` VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME'
        COMMENT '用工类型：FULL_TIME、PART_TIME' AFTER `record_uuid`,
    ADD COLUMN `hr_confirmed_at` DATETIME DEFAULT NULL
        COMMENT '人员分配审核通过时间' AFTER `submitted_at`,
    ADD COLUMN `hr_confirmed_by` INT DEFAULT NULL
        COMMENT '人员分配审核人' AFTER `hr_confirmed_at`,
    ADD KEY `idx_employee_assignment` (`employment_type`, `form_status`, `hr_confirmed_at`),
    ADD CONSTRAINT `fk_employee_hr_confirmed_by`
        FOREIGN KEY (`hr_confirmed_by`) REFERENCES `user` (`id`);
