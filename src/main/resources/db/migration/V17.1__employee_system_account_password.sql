-- 外部系统账号口令。明文可空，供人事回读；不是 user 表的登录密码。
ALTER TABLE `employee_system_account`
    ADD COLUMN `password` VARCHAR(200) DEFAULT NULL COMMENT '外部系统口令，明文，可空' AFTER `account_name`;
