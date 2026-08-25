package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetestSchemaMigrationTest {

    private static final String MIGRATION_RESOURCE = "db/migration/V4__create_retest_tables.sql";

    @Test
    void migrationAddsRecruitmentTypeAndCreatesRetestTables() throws IOException {
        String sql = readResource(MIGRATION_RESOURCE).toLowerCase();

        assertTrue(sql.contains("alter table `recruitment_info`"));
        assertTrue(sql.contains("information_schema.columns"));
        assertTrue(sql.contains("add column `recruitment_type`"));
        assertTrue(sql.contains("varchar(20)"));
        assertTrue(sql.contains("default ''internal''"));

        assertTrue(sql.contains("create table if not exists `retest_application`"));
        List.of("id", "record_uuid", "application_time", "applicant_remark",
                        "applicant_user_id", "created_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "retest_application must contain " + column));
        assertTrue(sql.contains("primary key (`id`)"));
        assertTrue(sql.contains("foreign key (`record_uuid`)"));
        assertTrue(sql.contains("references `recruitment_info` (`record_uuid`)"));
        assertTrue(sql.contains("foreign key (`applicant_user_id`)"));
        assertTrue(sql.contains("references `user` (`id`)"));

        assertTrue(sql.contains("create table if not exists `retest_review`"));
        List.of("id", "record_uuid", "retest_company", "retest_contact_person",
                        "retest_time", "reviewer_user_id", "review_time", "created_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "retest_review must contain " + column));
        assertTrue(sql.contains("foreign key (`reviewer_user_id`)"));
        assertFalse(sql.contains("create table if not exists `study2_user`"));
    }

    @Test
    void recruitmentTypeEnumHasStableDatabaseValues() throws Exception {
        Class<?> enumClass = Class.forName("com.example.study11.entity.enums.RecruitmentType");
        assertEquals(List.of("INTERNAL", "OUTSOURCED"),
                List.of(enumClass.getEnumConstants()).stream().map(value -> ((Enum<?>) value).name()).toList());
    }

    @Test
    void retestPosExposePersistenceFieldsWithExpectedTypes() throws Exception {
        Class<?> application = Class.forName("com.example.study11.entity.po.RetestApplicationPo");
        assertFieldType(application, "id", Long.class);
        assertFieldType(application, "recordUuid", String.class);
        assertFieldType(application, "applicationTime", LocalDateTime.class);
        assertFieldType(application, "applicantRemark", String.class);
        assertFieldType(application, "applicantUserId", Integer.class);
        assertFieldType(application, "createdAt", LocalDateTime.class);

        Class<?> review = Class.forName("com.example.study11.entity.po.RetestReviewPo");
        assertFieldType(review, "id", Long.class);
        assertFieldType(review, "recordUuid", String.class);
        assertFieldType(review, "retestCompany", String.class);
        assertFieldType(review, "retestContactPerson", String.class);
        assertFieldType(review, "retestTime", LocalDateTime.class);
        assertFieldType(review, "reviewerUserId", Integer.class);
        assertFieldType(review, "reviewTime", LocalDateTime.class);
        assertFieldType(review, "createdAt", LocalDateTime.class);
    }

    @Test
    void recruitmentInfoMapperPersistsRecruitmentType() throws IOException {
        String mapper = readResource("mapper/RecruitmentInfoMapper.xml").toLowerCase();
        assertTrue(mapper.contains("property=\"recruitmenttype\" column=\"recruitment_type\""));
        assertTrue(mapper.contains("recruitment_type"));
        assertTrue(mapper.contains("#{recruitmenttype}"));
        assertTrue(mapper.contains("recruitment_type = #{recruitmenttype}"));
    }

    @Test
    void recruitmentInfoPoAndVoExposeRecruitmentType() throws Exception {
        assertFieldType(Class.forName("com.example.study11.entity.po.RecruitmentInfoPo"),
                "recruitmentType", Class.forName("com.example.study11.entity.enums.RecruitmentType"));
        assertFieldType(Class.forName("com.example.study11.entity.vo.RecruitmentInfoVO"),
                "recruitmentType", Class.forName("com.example.study11.entity.enums.RecruitmentType"));
    }

    private static void assertFieldType(Class<?> type, String fieldName, Class<?> expectedType)
            throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        assertEquals(expectedType, field.getType(), fieldName + " has an unexpected type");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = RetestSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
