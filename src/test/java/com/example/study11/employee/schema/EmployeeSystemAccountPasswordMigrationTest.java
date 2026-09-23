package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeSystemAccountPasswordMigrationTest {

    @Test
    void migrationAddsNullablePasswordOnSystemAccountOnly() throws IOException {
        String sql = readResource("db/migration/V17.1__employee_system_account_password.sql").toLowerCase();

        assertTrue(sql.contains("alter table `employee_system_account`"));
        assertTrue(sql.contains("`password` varchar(200) default null"));
        assertFalse(sql.contains("not null"));
        assertFalse(sql.contains("`user`"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeSystemAccountPasswordMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
