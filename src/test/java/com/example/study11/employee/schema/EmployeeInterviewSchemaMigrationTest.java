package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeInterviewSchemaMigrationTest {

    @Test
    void migrationCreatesInterviewTableWithoutPassword() throws IOException {
        String sql = readResource("db/migration/V15__create_employee_interview.sql").toLowerCase();

        assertTrue(sql.contains("create table `employee_interview`"),
                "V15 must create employee_interview");
        List.of("interview_uuid", "employee_uuid", "interview_type", "interview_time",
                        "content", "handler_user_id", "created_by")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "employee_interview must contain " + column));

        assertTrue(sql.contains("primary key (`interview_uuid`)"));
        assertTrue(sql.contains("unique key `uk_employee_interview_id`"));
        assertTrue(sql.contains("idx_employee_interview_employee"));
        assertTrue(sql.contains("idx_employee_interview_handler"));
        assertTrue(sql.contains("references `employee` (`employee_uuid`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
        assertTrue(sql.contains("interview/retest/onboarding/customer_interview"));
        assertTrue(sql.contains("probation_confirm/salary_adjustment/return_to_office/departure"));

        assertFalse(sql.contains("password"));
        assertFalse(sql.contains("on delete cascade"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeInterviewSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
