package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobSchemaMigrationTest {

    @Test
    void migrationCreatesCompanyJobAllocationAndAuditTables() throws IOException {
        String sql = readResource("db/migration/V10__create_recruitment_job_tables.sql").toLowerCase();
        assertTrue(sql.contains("create table if not exists `recruitment_company`"));
        assertTrue(sql.contains("create table if not exists `recruitment_job`"));
        assertTrue(sql.contains("create table if not exists `recruitment_job_company`"));
        assertTrue(sql.contains("create table if not exists `recruitment_job_status_history`"));
        assertTrue(sql.contains("`job_uuid`              char(36)"));
        assertTrue(sql.contains("`id`                    bigint unsigned not null auto_increment"));
        assertTrue(sql.contains("unique key `uk_recruitment_job_id` (`id`)"));
        assertTrue(sql.contains("primary key (`job_uuid`)"));
        assertTrue(sql.contains("`is_deleted`            tinyint not null default 0"));
        assertTrue(sql.contains("unique key `uk_job_company_allocation` (`job_uuid`, `company_uuid`)"));
        assertTrue(sql.contains("`status`                varchar(20) not null default 'open' comment '岗位状态"));
        assertTrue(sql.contains("`status`               varchar(20) not null default 'open' comment '公司配额状态"));
        assertTrue(sql.contains("foreign key (`created_by_user_id`) references `user` (`id`)"));
        assertTrue(sql.contains("foreign key (`job_uuid`) references `recruitment_job` (`job_uuid`)"));
        assertTrue(sql.contains("foreign key (`company_uuid`) references `recruitment_company` (`company_uuid`)"));
        assertTrue(sql.contains("foreign key (`operator_user_id`) references `user` (`id`)"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = JobSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
