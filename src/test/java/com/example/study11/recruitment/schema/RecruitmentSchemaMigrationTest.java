package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitmentSchemaMigrationTest {

    private static final List<String> RECRUITMENT_COLUMNS = List.of(
            "record_uuid",
            "id",
            "applicant_name",
            "gender",
            "position",
            "phone",
            "email",
            "application_channel",
            "application_method",
            "status",
            "initial_contact_person",
            "initial_interview_time",
            "retest_contact_person",
            "retest_interview_time",
            "created_at",
            "updated_at"
    );

    @Test
    void migrationCreatesRecruitmentInfoWithoutReplacingStudy11User() throws IOException {
        String sql = readResource("db/migration/V1__create_recruitment_info.sql").toLowerCase();

        assertTrue(sql.contains("create table if not exists `recruitment_info`"));
        RECRUITMENT_COLUMNS.forEach(column ->
                assertTrue(sql.contains("`" + column + "`"),
                        "migration must contain recruitment column " + column));
        assertTrue(Pattern.compile("`id`[^,]*auto_increment", Pattern.DOTALL)
                .matcher(sql)
                .find(), "recruitment_info.id must be auto generated");
        assertFalse(sql.contains("primary key (`id`)") || sql.contains("primary key (id)"),
                "recruitment_info.id must not be the primary key");
        assertTrue(sql.contains("unique key"),
                "non-primary auto increment id must be indexed");
        assertFalse(sql.contains("drop table") || sql.contains("rename table `user`"),
                "study11 user table must not be dropped or renamed");
        assertFalse(sql.contains("create table if not exists `user`"),
                "study2 account table must not be copied into study11");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = RecruitmentSchemaMigrationTest.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
