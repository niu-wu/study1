-- Local test accounts for study11.
-- Not a Flyway migration. Re-run is safe: existing usernames are skipped.
-- HR/ADMIN cannot be created via /sso/register (that path always writes USER).
--
-- Common password Test@123 (8 chars, matches PasswordPolicy).
-- Compatibility password Admin@123 for zhangsan_admin only (old Apifox login).
-- Passwords are stored as BCrypt. Do not reuse these hashes in production.
--
-- Onboarding creates username = candidate phone. Do not reuse the phones below
-- as recruitment_info.phone, or POST /api/onboarding/process returns 409.

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'admin',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'ADMIN', NOW(), NOW(), 'admin@beihai.test', '13800000001', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'hr01',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'HR', NOW(), NOW(), 'hr01@beihai.test', '13800000002', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'hr01');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'hr02',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'HR', NOW(), NOW(), 'hr02@beihai.test', '13800000003', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'hr02');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'zhangsan',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'USER', NOW(), NOW(), '4654564@qq.com', '15066667778', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'zhangsan');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'lihong',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'USER', NOW(), NOW(), 'lihong@beihai.test', '13800000004', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'lihong');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'wangxiaoqiang',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'USER', NOW(), NOW(), 'wxq@beihai.test', '13800000005', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'wangxiaoqiang');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'pm01',
       '$2a$10$a9CpjOGY4n58f2dfBmKnwu0Fuoa4bsNecpQ3SewRYTZtOd6M/Ki9u',
       'USER', NOW(), NOW(), 'pm01@beihai.test', '13800000006', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'pm01');

INSERT INTO `user` (`username`, `password`, `role`, `created_at`, `updatetime`, `email`, `phone`, `status`, `is_deleted`)
SELECT 'zhangsan_admin',
       '$2a$10$Vv23iJusUTKkxV0H02EffeFeMtI55jZK0SP/0TbHxWjwsQjxDXntS',
       'ADMIN', NOW(), NOW(), 'zhangsan_admin@beihai.test', '13700000001', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'zhangsan_admin');
