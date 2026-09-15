package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeArchiveSchemaMigrationTest {

    @Test
    void migrationAddsArchiveColumnsAndEventSalaryAccountTables() throws IOException {
        String sql = readResource("db/migration/V14__employee_archive.sql").toLowerCase();

        List.of("department", "company_email", "employment_status", "work_location", "hired_at",
                        "probation_end_date", "regularized_at", "customer_name", "contract_salary",
                        "probation_salary", "training_result")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "V14 must add " + column));

        List.of("employee_salary_record", "employee_assignment_record", "employee_system_account")
                .forEach(table -> assertTrue(sql.contains("create table if not exists `" + table + "`"),
                        "V14 must create " + table));

        assertTrue(sql.contains("default 'probation'"));
        assertTrue(sql.contains("default 'headquarters'"));
        assertTrue(sql.contains("set `employment_status` = 'regular'"));
        assertTrue(sql.contains("= 'part_time'"));
        assertTrue(sql.contains("onboarding_record"));
        assertTrue(sql.contains("date(e.created_at)"));

        assertTrue(sql.contains("idx_employee_status_location"));
        assertTrue(sql.contains("idx_employee_full_name"));
        assertTrue(sql.contains("idx_employee_phone"));
        assertTrue(sql.contains("idx_employee_customer_name"));
        assertTrue(sql.contains("idx_employee_position"));
        assertTrue(sql.contains("uk_employee_salary_month"));
        assertTrue(sql.contains("`salary_month`"));
        assertTrue(sql.contains("`scheduled_days`"));
        assertTrue(sql.contains("`actual_days`"));
        assertTrue(sql.contains("`leave_days`"));
        assertTrue(sql.contains("`gross_pay`"));
        assertTrue(sql.contains("`net_pay`"));
        assertTrue(sql.contains("`event_date`"));
        assertTrue(sql.contains("`assignment_type`"));
        assertTrue(sql.contains("`utilization_rate`"));
        assertTrue(sql.contains("`system_name`"));
        assertTrue(sql.contains("`account_name`"));
        assertTrue(sql.contains("references `employee` (`employee_uuid`)"));
        assertTrue(sql.contains("references `user` (`id`)"));

        String assignmentSql = compact(tableSql(sql, "employee_assignment_record"));
        assertTrue(assignmentSql.contains("`event_date` date not null"));
        assertFalse(assignmentSql.contains("`start_date`"));
        assertFalse(assignmentSql.contains("`end_date`"));
        assertFalse(assignmentSql.contains("`duration_text`"));

        String salarySql = compact(tableSql(sql, "employee_salary_record"));
        assertTrue(salarySql.contains("unique key `uk_employee_salary_month` (`employee_uuid`, `salary_month`)"));
        assertTrue(salarySql.contains("`scheduled_days` int default null"));
        assertTrue(salarySql.contains("`actual_days` int default null"));
        assertTrue(salarySql.contains("`leave_days` int default null"));
        assertTrue(salarySql.contains("`base_salary` decimal(12, 2) not null default 0.00"));
        assertTrue(salarySql.contains("`gross_pay` decimal(12, 2) not null"));
        assertTrue(salarySql.contains("`net_pay` decimal(12, 2) not null"));
        assertTrue(salarySql.contains("`tax_rate` decimal(5, 2) default null"));
        assertFalse(salarySql.contains("`scheduled_days` int not null"));
        assertFalse(salarySql.contains("password"));

        String accountSql = compact(tableSql(sql, "employee_system_account"));
        assertFalse(accountSql.contains("password"));
        assertFalse(sql.contains("password"));
        assertFalse(sql.contains("`start_date`"));
        assertFalse(sql.contains("`end_date`"));
        assertFalse(sql.contains("`duration_text`"));
    }

    private static String compact(String sql) {
        return sql.replaceAll("\\s+", " ");
    }

    private static String tableSql(String sql, String table) {
        String marker = "create table if not exists `" + table + "`";
        int start = sql.indexOf(marker);
        assertTrue(start >= 0, "missing table " + table);
        int nextCreate = sql.indexOf("create table if not exists `", start + marker.length());
        int end = nextCreate >= 0 ? nextCreate : sql.length();
        return sql.substring(start, end);
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeArchiveSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
