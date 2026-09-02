package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleMigrationTest {

    @Test
    void migrationAddsBackendOwnedRoleWithUserDefault() throws IOException {
        String sql = readResource("db/migration/V9__add_user_role.sql").toUpperCase();
        assertTrue(sql.contains("ADD COLUMN `ROLE` VARCHAR(20)"));
        assertTrue(sql.contains("NOT NULL DEFAULT 'USER'"));
        assertTrue(sql.contains("USER, HR, ADMIN"));
        assertTrue(sql.contains("CREATE INDEX `IDX_USER_ROLE`"));
        assertTrue(!sql.contains("UPDATE `USER`"), "migration must not overwrite existing account roles");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = UserRoleMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
