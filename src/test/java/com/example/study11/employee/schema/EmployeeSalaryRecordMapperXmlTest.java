package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeSalaryRecordMapperXmlTest {

    @Test
    void salaryQueriesOrderByMonthDescAndDoNotMentionPassword() throws IOException {
        String xml = readResource("mapper/EmployeeSalaryRecordMapper.xml").toLowerCase();

        assertTrue(xml.contains("order by salary_month desc, id desc"));
        assertTrue(xml.contains("`scheduled_days`") || xml.contains("scheduled_days"));
        assertFalse(xml.contains("password"));
        assertFalse(xml.contains("attendance"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeSalaryRecordMapperXmlTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing mapper resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
