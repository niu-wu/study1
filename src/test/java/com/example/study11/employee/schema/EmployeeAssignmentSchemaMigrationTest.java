package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeAssignmentSchemaMigrationTest {

    @Test
    void migrationAddsAssignmentAndPartTimeColumnsWithoutNewBusinessTables() throws IOException {
        String sql = readResource("db/migration/V13__employee_assignment_and_part_time.sql").toLowerCase();

        List.of("employment_type", "hr_confirmed_at", "hr_confirmed_by")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "V13 must add " + column));
        assertTrue(sql.contains("modify `record_uuid`"));
        assertTrue(sql.contains("default 'full_time'"));
        assertTrue(sql.contains("idx_employee_assignment"));
        assertTrue(sql.contains("references `user` (`id`)"));

        assertFalse(sql.contains("create table"));
        assertFalse(sql.contains("employee_contract"));
        assertFalse(sql.contains("employee_salary"));
        assertFalse(sql.contains("employee_attachment"));
        assertFalse(sql.contains("password"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeAssignmentSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
