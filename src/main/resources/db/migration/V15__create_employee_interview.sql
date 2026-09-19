-- 员工面谈记录。一条记录 = 一次面谈交流，content 存富文本 HTML（入库前由服务端 Jsoup 清洗）。
-- handler_user_id 可空：系统自动产生的事件（如转正/调薪流程触发）可不记经办人；HR 手动登记时写当前登录人。
CREATE TABLE `employee_interview` (
    `interview_uuid` char(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '面谈记录业务主键',
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库自增编号，非业务主键',
    `employee_uuid` char(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '所属员工档案',
    `interview_type` varchar(30) NOT NULL COMMENT '面谈类型：INTERVIEW/RETEST/ONBOARDING/CUSTOMER_INTERVIEW/PROBATION_CONFIRM/SALARY_ADJUSTMENT/RETURN_TO_OFFICE/DEPARTURE',
    `interview_time` datetime NOT NULL COMMENT '面谈时间',
    `content` longtext COMMENT '面谈内容，富文本 HTML，入库前经 Jsoup 清洗',
    `handler_user_id` int DEFAULT NULL COMMENT '经办人，关联 user.id；可空',
    `created_by` int DEFAULT NULL COMMENT '创建人，关联 user.id',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`interview_uuid`),
    UNIQUE KEY `uk_employee_interview_id` (`id`),
    KEY `idx_employee_interview_employee` (`employee_uuid`),
    KEY `idx_employee_interview_handler` (`handler_user_id`),
    CONSTRAINT `fk_employee_interview_employee` FOREIGN KEY (`employee_uuid`) REFERENCES `employee` (`employee_uuid`),
    CONSTRAINT `fk_employee_interview_handler` FOREIGN KEY (`handler_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_employee_interview_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工面谈交流记录';
