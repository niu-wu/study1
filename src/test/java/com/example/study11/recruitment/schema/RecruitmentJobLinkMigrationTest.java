package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitmentJobLinkMigrationTest {

    @Test
    void migrationAddsNullableJobAndAllocationReferencesWithoutRemovingPositionSnapshot() throws IOException {
        String sql = readResource("db/migration/V11__link_recruitment_info_to_job.sql").toLowerCase();
        assertTrue(sql.contains("add column `job_uuid` char(36)"));
        assertTrue(sql.contains("add column `job_company_allocation_uuid` char(36)"));
        assertTrue(sql.contains("null comment"));
        assertTrue(sql.contains("foreign key (`job_uuid`) references `recruitment_job` (`job_uuid`)"));
        assertTrue(sql.contains("foreign key (`job_company_allocation_uuid`) references `recruitment_job_company` (`allocation_uuid`)"));
        assertTrue(!sql.contains("drop column `position`"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = RecruitmentJobLinkMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
