package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeArchiveQueryXmlTest {

    @Test
    void archiveQueriesUseConfirmedScopePrefixMatchAndGlobalStatistics() throws IOException {
        String xml = readResource("mapper/EmployeeMapper.xml");

        assertTrue(xml.contains("hr_confirmed_at IS NOT NULL"));
        assertTrue(xml.contains("full_name LIKE CONCAT(#{request.fullName}, '%')"));
        assertTrue(xml.contains("phone LIKE CONCAT(#{request.phone}, '%')"));
        assertTrue(xml.contains("customer_name LIKE CONCAT(#{request.customerName}, '%')"));
        assertTrue(xml.contains("position LIKE CONCAT(#{request.position}, '%')"));
        assertFalse(xml.contains("LIKE CONCAT('%', #{request.fullName}"));
        assertFalse(xml.contains("LIKE CONCAT('%', #{request.phone}"));
        assertFalse(xml.contains("LIKE CONCAT('%', #{request.customerName}"));
        assertFalse(xml.contains("LIKE CONCAT('%', #{request.position}"));

        assertTrue(xml.contains("employment_status = 'PROBATION'"));
        assertTrue(xml.contains("employment_status != 'RESIGNED'"));
        assertTrue(xml.contains("work_location = 'HEADQUARTERS'"));
        assertTrue(xml.contains("work_location = 'DISPATCHED'"));
        assertTrue(xml.contains("employment_status = 'RESIGNED'"));

        int statsStart = xml.indexOf("id=\"selectArchiveStatistics\"");
        assertTrue(statsStart >= 0, "missing selectArchiveStatistics");
        int statsEnd = xml.indexOf("</select>", statsStart);
        String statsSql = xml.substring(statsStart, statsEnd);
        assertFalse(statsSql.contains("#{request."), "statistics must ignore list filters");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeArchiveQueryXmlTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing mapper resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
