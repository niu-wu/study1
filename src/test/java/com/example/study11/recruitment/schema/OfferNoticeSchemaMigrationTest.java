package com.example.study11.recruitment.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfferNoticeSchemaMigrationTest {

    @Test
    void migrationCreatesDraftAndSentNoticeFieldsAndReferencesExistingUser() throws IOException {
        String sql = readResource("db/migration/V5__create_offer_notice.sql").toLowerCase();

        assertTrue(sql.contains("create table if not exists `offer_notice`"));
        List.of("id", "record_uuid", "recipient_email", "notice_content", "status",
                        "drafted_by_user_id", "drafted_at", "sent_by_user_id", "sent_at",
                        "created_at", "updated_at")
                .forEach(column -> assertTrue(sql.contains("`" + column + "`"),
                        "offer_notice must contain " + column));
        assertTrue(sql.contains("primary key (`id`)"));
        assertTrue(sql.contains("unique key `uk_offer_notice_record_uuid`"));
        assertTrue(sql.contains("foreign key (`record_uuid`)"));
        assertTrue(sql.contains("references `recruitment_info` (`record_uuid`)"));
        assertTrue(sql.contains("foreign key (`drafted_by_user_id`)"));
        assertTrue(sql.contains("foreign key (`sent_by_user_id`)"));
        assertTrue(sql.contains("references `user` (`id`)"));
        assertTrue(sql.contains("default 'draft'"));
        assertTrue(sql.contains("check (`status` in ('draft', 'sent'))"));
    }

    @Test
    void offerNoticePoUsesStablePersistenceTypes() throws Exception {
        Class<?> type = Class.forName("com.example.study11.entity.po.OfferNoticePo");
        assertFieldType(type, "id", Long.class);
        assertFieldType(type, "recordUuid", String.class);
        assertFieldType(type, "recipientEmail", String.class);
        assertFieldType(type, "noticeContent", String.class);
        assertFieldType(type, "status", String.class);
        assertFieldType(type, "draftedByUserId", Integer.class);
        assertFieldType(type, "draftedAt", LocalDateTime.class);
        assertFieldType(type, "sentByUserId", Integer.class);
        assertFieldType(type, "sentAt", LocalDateTime.class);
    }

    private static void assertFieldType(Class<?> type, String fieldName, Class<?> expectedType)
            throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        assertEquals(expectedType, field.getType(), fieldName + " has an unexpected type");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = OfferNoticeSchemaMigrationTest.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            assertNotNull(stream, "Missing migration resource: " + resourceName);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
