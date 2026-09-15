package com.example.study11.employee.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeSchemaMigrationTest {

    @Test
    void migrationCreatesSixOnboardingTablesWithWechatOnEmployee() throws IOException {
        String sql = readResource("db/migration/V12__create_employee_onboarding_tables.sql").toLowerCase();

        List.of("employee", "employee_education", "employee_work_history",
                        "employee_training", "employee_family", "employee_emergency_contact")
                .forEach(table -> assertTrue(sql.contains("create table if not exists `" + table + "`"),
                        "V12 must create " + table));

        List.of("employee_uuid", "employee_no", "user_id", "record_uuid", "full_name", "gender",
                        "birth_date", "position", "phone", "email", "id_card", "marital_status",
                        "political_status", "nationality", "ethnicity", "native_place",
                        "hukou_location", "current_address", "postal_code", "health_status",
                        "highest_education", "major", "professional_title", "foreign_language",
                        "hobbies", "photo_path", "wechat_account", "wechat_openid", "wechat_unionid",
                        "wechat_bound_at", "form_status", "submitted_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "employee must contain " + column));

        assertTrue(sql.contains("primary key (`employee_uuid`)"));
        assertTrue(sql.contains("unique key `uk_employee_id`"));
        assertTrue(sql.contains("unique key `uk_employee_no`"));
        assertTrue(sql.contains("unique key `uk_employee_user_id`"));
        assertTrue(sql.contains("unique key `uk_employee_record_uuid`"));
        assertTrue(sql.contains("default 'draft'"));
        assertTrue(sql.contains("references `onboarding_record` (`record_uuid`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
        assertTrue(sql.contains("references `employee` (`employee_uuid`)"));

        assertTrue(sql.contains("`certificate`"));
        assertTrue(sql.contains("`leave_reason`"));
        assertTrue(sql.contains("`reference_name`"));
        assertTrue(sql.contains("`reference_phone`"));
        assertTrue(sql.contains("`work_unit`"));
        assertTrue(sql.contains("`job_title`"));
        assertTrue(sql.contains("`address`"));

        assertFalse(sql.contains("create table if not exists `employee_wechat`"));
        assertFalse(sql.contains("create table if not exists `employee_attachment`"));
        assertFalse(sql.contains("on delete cascade"));
        assertFalse(sql.contains("password"));
        assertFalse(sql.contains("`current_address_2`"));
        assertFalse(sql.contains("`hukou_location_2`"));
    }

    @Test
    void familyAndEmergencyContactMustKeepDifferentColumns() throws IOException {
        String sql = readResource("db/migration/V12__create_employee_onboarding_tables.sql").toLowerCase();
        int familyStart = sql.indexOf("create table if not exists `employee_family`");
        int emergencyStart = sql.indexOf("create table if not exists `employee_emergency_contact`");
        assertTrue(familyStart >= 0 && emergencyStart > familyStart);

        String familySql = sql.substring(familyStart, emergencyStart);
        String emergencySql = sql.substring(emergencyStart);

        assertTrue(familySql.contains("`work_unit`"));
        assertTrue(familySql.contains("`job_title`"));
        assertFalse(familySql.contains("`phone`"), "family members do not have phone on the prototype");
        assertFalse(familySql.contains("`address`"));

        assertTrue(emergencySql.contains("`address`"));
        assertTrue(emergencySql.contains("`postal_code`"));
        assertTrue(emergencySql.contains("`phone`"));
        assertFalse(emergencySql.contains("`work_unit`"));
        assertFalse(emergencySql.contains("`job_title`"));
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = EmployeeSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
