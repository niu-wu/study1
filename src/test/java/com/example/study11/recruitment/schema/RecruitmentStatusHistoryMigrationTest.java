package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitmentStatusHistoryMigrationTest {

    @Test
    void migrationCreatesHistoryTableWithRecruitmentAndUserReferences() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/"
                + "V2__create_recruitment_status_history.sql"), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("create table if not exists `recruitment_status_history`"));
        assertTrue(sql.contains("primary key (`id`)"));
        assertTrue(sql.contains("foreign key (`record_uuid`)"));
        assertTrue(sql.contains("references `recruitment_info` (`record_uuid`)"));
        assertTrue(sql.contains("foreign key (`operator_user_id`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
    }
}
