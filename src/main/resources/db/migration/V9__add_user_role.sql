-- Add backend-owned account role. Existing accounts keep the default USER role.
ALTER TABLE `user`
    ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'User role: USER, HR, ADMIN' AFTER `password`;

CREATE INDEX `idx_user_role` ON `user` (`role`);
