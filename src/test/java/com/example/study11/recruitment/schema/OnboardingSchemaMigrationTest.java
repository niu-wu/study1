package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnboardingSchemaMigrationTest {

    @Test
    void migrationCreatesUserLinkedOnboardingRecordWithoutPasswordColumn() throws IOException {
        String sql = readResource("db/migration/V7__create_onboarding_record.sql").toLowerCase();

        assertTrue(sql.contains("create table if not exists `onboarding_record`"));
        List.of("id", "record_uuid", "user_id", "onboarding_date", "onboarding_note",
                        "processed_by_user_id", "created_at", "updated_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "onboarding_record must contain " + column));
        assertTrue(sql.contains("primary key (`id`)"));
        assertTrue(sql.contains("unique key `uk_onboarding_record_uuid`"));
        assertTrue(sql.contains("unique key `uk_onboarding_user_id`"));
        assertTrue(sql.contains("references `recruitment_info` (`record_uuid`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
        assertTrue(!sql.contains("password"), "initial password must never be stored");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = OnboardingSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
