package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RejectionSchemaMigrationTest {

    @Test
    void migrationCreatesRejectionTableWithRecruitmentUserAndResumeReferences() throws IOException {
        String sql = readResource("db/migration/V6__create_recruitment_rejection.sql").toLowerCase();

        assertTrue(sql.contains("create table if not exists `recruitment_rejection`"));
        List.of("id", "record_uuid", "action", "rejection_stage", "rejection_reason", "remark",
                        "resume_attachment_id", "talent_category", "operator_user_id", "rejection_time",
                        "created_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "recruitment_rejection must contain " + column));
        assertTrue(sql.contains("primary key (`id`)"));
        assertTrue(sql.contains("unique key `uk_recruitment_rejection_record_uuid`"));
        assertTrue(sql.contains("references `recruitment_info` (`record_uuid`)"));
        assertTrue(sql.contains("references `candidate_resume` (`id`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
        assertTrue(sql.contains("check (`action` in ('reject', 'decline'))"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = RejectionSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
