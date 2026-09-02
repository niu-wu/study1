-- Link candidate recruitment records to the independent job and company allocation tables.
-- Existing rows remain valid with nullable associations; position stays as a historical snapshot.
ALTER TABLE `recruitment_info`
    ADD COLUMN `job_uuid` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '岗位业务标识' AFTER `position`,
    ADD COLUMN `job_company_allocation_uuid` CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '岗位公司配额业务标识' AFTER `job_uuid`,
    ADD KEY `idx_recruitment_info_job_uuid` (`job_uuid`),
    ADD KEY `idx_recruitment_info_allocation_uuid` (`job_company_allocation_uuid`),
    ADD CONSTRAINT `fk_recruitment_info_job`
        FOREIGN KEY (`job_uuid`) REFERENCES `recruitment_job` (`job_uuid`),
    ADD CONSTRAINT `fk_recruitment_info_allocation`
        FOREIGN KEY (`job_company_allocation_uuid`) REFERENCES `recruitment_job_company` (`allocation_uuid`);
