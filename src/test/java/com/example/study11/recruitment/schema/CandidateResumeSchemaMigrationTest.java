package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateResumeSchemaMigrationTest {

    private static final String MIGRATION_RESOURCE = "db/migration/V3__create_candidate_resume.sql";
    private static final String MAPPER_RESOURCE = "mapper/CandidateResumeMapper.xml";

    @Test
    void migrationCreatesResumeMetadataTableBoundToRecruitmentAndUploader() throws IOException {
        String sql = readResource(MIGRATION_RESOURCE).toLowerCase();

        assertTrue(sql.contains("create table if not exists `candidate_resume`"));
        List.of("id", "record_uuid", "original_filename", "stored_filename", "file_size",
                        "content_type", "uploader_user_id", "uploaded_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "migration must contain candidate_resume column " + column));
        assertTrue(Pattern.compile("`id`\\s+bigint unsigned\\s+not null\\s+auto_increment",
                        Pattern.DOTALL)
                .matcher(sql)
                .find());
        assertTrue(Pattern.compile("`record_uuid`\\s+char\\(36\\)\\s+character set ascii"
                        + "\\s+collate ascii_bin\\s+not null", Pattern.DOTALL)
                .matcher(sql)
                .find());
        assertTrue(Pattern.compile("`uploader_user_id`\\s+int\\s+not null", Pattern.DOTALL)
                .matcher(sql)
                .find());
        assertTrue(sql.contains("primary key (`id`)"));
        assertTrue(sql.contains("unique key `uk_candidate_resume_stored_filename` (`stored_filename`)"));
        assertTrue(sql.contains("key `idx_candidate_resume_record_uuid` (`record_uuid`)"));
        assertTrue(sql.contains("key `idx_candidate_resume_uploader_user_id` (`uploader_user_id`)"));
        assertTrue(sql.contains("constraint `chk_candidate_resume_file_size` check (`file_size` > 0)"));
        assertTrue(sql.contains("foreign key (`record_uuid`)"));
        assertTrue(sql.contains("references `recruitment_info` (`record_uuid`)"));
        assertTrue(sql.contains("foreign key (`uploader_user_id`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
        assertFalse(sql.contains("create table if not exists `user`"));
        assertFalse(sql.contains("password"));
        assertFalse(sql.contains("storage_path"));
        assertFalse(sql.contains("on delete cascade"));
    }

    @Test
    void persistenceContractKeepsStorageIdentifiersInternalToThePoAndMapper() throws Exception {
        Class<?> resumePoClass = Class.forName("com.example.study11.entity.po.CandidateResumePo");
        assertFieldType(resumePoClass, "id", Long.class);
        assertFieldType(resumePoClass, "recordUuid", String.class);
        assertFieldType(resumePoClass, "originalFilename", String.class);
        assertFieldType(resumePoClass, "storedFilename", String.class);
        assertFieldType(resumePoClass, "fileSize", Long.class);
        assertFieldType(resumePoClass, "contentType", String.class);
        assertFieldType(resumePoClass, "uploaderUserId", Integer.class);
        assertFieldType(resumePoClass, "uploadedAt", LocalDateTime.class);

        Class<?> resumeVoClass = Class.forName("com.example.study11.entity.vo.CandidateResumeVO");
        assertFieldType(resumeVoClass, "id", Long.class);
        assertFieldType(resumeVoClass, "recordUuid", String.class);
        assertFieldType(resumeVoClass, "originalFilename", String.class);
        assertFieldType(resumeVoClass, "fileSize", Long.class);
        assertFieldType(resumeVoClass, "contentType", String.class);
        assertFieldType(resumeVoClass, "uploaderUserId", Integer.class);
        assertFieldType(resumeVoClass, "uploadedAt", LocalDateTime.class);
        assertFalse(hasField(resumeVoClass, "storedFilename"));
        assertFalse(hasField(resumeVoClass, "storagePath"));

        String mapperXml = readResource(MAPPER_RESOURCE).toLowerCase();
        assertTrue(mapperXml.contains("namespace=\"com.example.study11.dao.candidateresumedao\""));
        assertTrue(mapperXml.contains("<insert id=\"insert\""));
        assertTrue(mapperXml.contains("<select id=\"selectbyid\""));
        assertTrue(mapperXml.contains("<select id=\"selectbyrecorduuid\""));
        assertTrue(mapperXml.contains("<delete id=\"deletebyid\""));
        assertTrue(mapperXml.contains("from `candidate_resume`"));
        assertFalse(mapperXml.contains("from `user`"));
        assertFalse(mapperXml.contains("from `recruitment_info`"));

        int insertStart = mapperXml.indexOf("<insert id=\"insert\"");
        int insertEnd = mapperXml.indexOf("</insert>", insertStart);
        String insertSql = mapperXml.substring(insertStart, insertEnd);
        assertFalse(insertSql.contains("uploaded_at"),
                "the database default must create upload time when metadata is inserted");
        assertFalse(insertSql.contains("#{uploadedat}"),
                "the mapper must not insert a null upload time explicitly");
    }

    private static void assertFieldType(Class<?> type, String fieldName, Class<?> expectedType)
            throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        assertEquals(expectedType, field.getType(), fieldName + " has an unexpected type");
    }

    private static boolean hasField(Class<?> type, String fieldName) {
        try {
            type.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException exception) {
            return false;
        }
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = CandidateResumeSchemaMigrationTest.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
