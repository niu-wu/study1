-- Repair mojibake comments introduced by an earlier database export/import.
-- This migration changes metadata comments only; column definitions and data remain unchanged.

ALTER TABLE `user`
    MODIFY COLUMN `id` INT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    MODIFY COLUMN `username` VARCHAR(50) NOT NULL COMMENT 'Username',
    MODIFY COLUMN `password` VARCHAR(255) NOT NULL COMMENT 'Password hash',
    MODIFY COLUMN `created_at` DATETIME NOT NULL COMMENT 'Creation time',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 1 COMMENT 'Account status: 1 active, 2 disabled',
    MODIFY COLUMN `is_deleted` INT NOT NULL DEFAULT 0 COMMENT 'Logical deletion: 0 active, 1 deleted',
    MODIFY COLUMN `updatetime` DATETIME DEFAULT NULL COMMENT 'Last update time',
    MODIFY COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email address',
    MODIFY COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone number',
    MODIFY COLUMN `birthday` DATE DEFAULT NULL COMMENT 'Birthday',
    COMMENT = 'User account table';

ALTER TABLE `recruitment_status_history`
    MODIFY COLUMN `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '状态历史编号',
    MODIFY COLUMN `record_uuid` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '招聘记录业务标识',
    MODIFY COLUMN `from_status` VARCHAR(20) NOT NULL COMMENT '变更前状态',
    MODIFY COLUMN `to_status` VARCHAR(20) NOT NULL COMMENT '变更后状态',
    MODIFY COLUMN `action` VARCHAR(40) NOT NULL COMMENT '状态变更动作',
    MODIFY COLUMN `operator_user_id` INT NOT NULL COMMENT '操作人用户编号',
    MODIFY COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '状态变更备注',
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    COMMENT = '招聘状态历史表';
