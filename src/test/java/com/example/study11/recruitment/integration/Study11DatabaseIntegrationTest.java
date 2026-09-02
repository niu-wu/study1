package com.example.study11.recruitment.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Optional integration contract for the real study11 MySQL schema.
 *
 * The suite is disabled for ordinary unit-test runs. Enable it explicitly with
 * STUDY11_INTEGRATION_TESTS=true and provide the database connection variables
 * when the local MySQL instance is available.
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "STUDY11_INTEGRATION_TESTS", matches = "true")
class Study11DatabaseIntegrationTest {

    private static final String SCHEMA = env("STUDY11_DB_SCHEMA", "study11");

    private static final String JDBC_URL = env("STUDY11_DB_URL",
            "jdbc:mysql://localhost:3306/" + SCHEMA
                    + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");

    private static final String JDBC_USER = env("STUDY11_DB_USER", "root");

    private static final String JDBC_PASSWORD = env("STUDY11_DB_PASSWORD", "");

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "user",
            "recruitment_info",
            "recruitment_status_history",
            "candidate_resume",
            "retest_application",
            "retest_review",
            "offer_notice",
            "recruitment_rejection",
            "onboarding_record",
            "recruitment_company",
            "recruitment_job",
            "recruitment_job_company",
            "recruitment_job_status_history");

    private static final Map<String, Set<String>> EXPECTED_PRIMARY_KEYS = Map.ofEntries(
            Map.entry("recruitment_info", Set.of("record_uuid")),
            Map.entry("recruitment_status_history", Set.of("id")),
            Map.entry("candidate_resume", Set.of("id")),
            Map.entry("retest_application", Set.of("id")),
            Map.entry("retest_review", Set.of("id")),
            Map.entry("offer_notice", Set.of("id")),
            Map.entry("recruitment_rejection", Set.of("id")),
            Map.entry("onboarding_record", Set.of("id")),
            Map.entry("recruitment_company", Set.of("company_uuid")),
            Map.entry("recruitment_job", Set.of("job_uuid")),
            Map.entry("recruitment_job_company", Set.of("allocation_uuid")),
            Map.entry("recruitment_job_status_history", Set.of("id")));

    private static final Set<ForeignKey> EXPECTED_FOREIGN_KEYS = Set.of(
            new ForeignKey("recruitment_status_history", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("recruitment_status_history", "operator_user_id", "user", "id"),
            new ForeignKey("candidate_resume", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("candidate_resume", "uploader_user_id", "user", "id"),
            new ForeignKey("retest_application", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("retest_application", "applicant_user_id", "user", "id"),
            new ForeignKey("retest_review", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("retest_review", "reviewer_user_id", "user", "id"),
            new ForeignKey("offer_notice", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("offer_notice", "drafted_by_user_id", "user", "id"),
            new ForeignKey("offer_notice", "sent_by_user_id", "user", "id"),
            new ForeignKey("recruitment_rejection", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("recruitment_rejection", "resume_attachment_id", "candidate_resume", "id"),
            new ForeignKey("recruitment_rejection", "operator_user_id", "user", "id"),
            new ForeignKey("onboarding_record", "record_uuid", "recruitment_info", "record_uuid"),
            new ForeignKey("onboarding_record", "user_id", "user", "id"),
            new ForeignKey("onboarding_record", "processed_by_user_id", "user", "id"),
            new ForeignKey("recruitment_company", "created_by_user_id", "user", "id"),
            new ForeignKey("recruitment_job", "created_by_user_id", "user", "id"),
            new ForeignKey("recruitment_job", "updated_by_user_id", "user", "id"),
            new ForeignKey("recruitment_job_company", "job_uuid", "recruitment_job", "job_uuid"),
            new ForeignKey("recruitment_job_company", "company_uuid", "recruitment_company", "company_uuid"),
            new ForeignKey("recruitment_job_company", "created_by_user_id", "user", "id"),
            new ForeignKey("recruitment_job_company", "updated_by_user_id", "user", "id"),
            new ForeignKey("recruitment_job_status_history", "job_uuid", "recruitment_job", "job_uuid"),
            new ForeignKey("recruitment_job_status_history", "allocation_uuid", "recruitment_job_company", "allocation_uuid"),
            new ForeignKey("recruitment_job_status_history", "operator_user_id", "user", "id"),
            new ForeignKey("recruitment_info", "job_uuid", "recruitment_job", "job_uuid"),
            new ForeignKey("recruitment_info", "job_company_allocation_uuid", "recruitment_job_company", "allocation_uuid"));

    private Connection connection;

    @BeforeEach
    void openConnection() throws SQLException {
        connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    @AfterEach
    void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void expectedTablesAndUserColumnsArePresent() throws SQLException {
        Set<String> actualTables = queryStringSet(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = ? AND table_type = 'BASE TABLE'", SCHEMA);
        assertTrue(actualTables.containsAll(EXPECTED_TABLES),
                "study11 is missing one or more required tables");

        Set<String> expectedUserColumns = Set.of(
                "id", "username", "password", "role", "created_at", "status",
                "is_deleted", "updatetime", "email", "phone", "birthday");
        Set<String> actualUserColumns = queryStringSet(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema = ? AND table_name = 'user'", SCHEMA);
        assertEquals(expectedUserColumns, actualUserColumns,
                "user must remain an account/user table and must not gain recruitment fields");
    }

    @Test
    void primaryKeysAndRequiredUniqueIndexesArePreserved() throws SQLException {
        for (Map.Entry<String, Set<String>> entry : EXPECTED_PRIMARY_KEYS.entrySet()) {
            assertEquals(entry.getValue(), primaryKeyColumns(entry.getKey()),
                    "unexpected primary key for " + entry.getKey());
        }

        Map<String, Set<String>> uniqueIndexes = Map.of(
                "recruitment_info.uk_recruitment_info_id", Set.of("id"),
                "retest_application.uk_retest_application_record_uuid", Set.of("record_uuid"),
                "retest_review.uk_retest_review_record_uuid", Set.of("record_uuid"),
                "offer_notice.uk_offer_notice_record_uuid", Set.of("record_uuid"),
                "recruitment_rejection.uk_recruitment_rejection_record_uuid", Set.of("record_uuid"),
                "onboarding_record.uk_onboarding_record_uuid", Set.of("record_uuid"),
                "onboarding_record.uk_onboarding_user_id", Set.of("user_id"),
                "candidate_resume.uk_candidate_resume_stored_filename", Set.of("stored_filename"));
        for (Map.Entry<String, Set<String>> entry : uniqueIndexes.entrySet()) {
            String[] name = entry.getKey().split("\\.", 2);
            assertEquals(entry.getValue(), indexColumns(name[0], name[1]),
                    "unexpected unique index " + entry.getKey());
        }
    }

    @Test
    void foreignKeysStatusDefaultsAndAutoIncrementColumnsAreValid() throws SQLException {
        Set<ForeignKey> actualForeignKeys = queryForeignKeys();
        assertTrue(actualForeignKeys.containsAll(EXPECTED_FOREIGN_KEYS),
                "one or more recruitment foreign keys are missing");

        assertEquals("PENDING_INITIAL", columnDefault("recruitment_info", "status"));
        assertEquals("INTERNAL", columnDefault("recruitment_info", "recruitment_type"));
        assertEquals("DRAFT", columnDefault("offer_notice", "status"));
        assertEquals("USER", columnDefault("user", "role"));

        for (String table : EXPECTED_PRIMARY_KEYS.keySet()) {
            assertTrue(columnExtra(table, "id").contains("auto_increment"),
                    table + ".id must be database generated");
        }
        assertTrue(columnExtra("user", "id").contains("auto_increment"),
                "user.id must remain database generated");
    }

    @Test
    void recruitmentIdIsGeneratedAndIsNotThePrimaryKey() throws SQLException {
        String recordUuid = UUID.randomUUID().toString();
        try {
            long generatedId;
            String insertSql = "INSERT INTO recruitment_info "
                    + "(record_uuid, applicant_name, position) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(
                    insertSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, recordUuid);
                statement.setString(2, "database integration candidate");
                statement.setString(3, "database integration position");
                assertEquals(1, statement.executeUpdate());
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    assertTrue(keys.next(), "MySQL must return the generated recruitment id");
                    generatedId = keys.getLong(1);
                }
            }

            assertTrue(generatedId > 0, "recruitment_info.id must be generated");
            assertEquals(Set.of("record_uuid"), primaryKeyColumns("recruitment_info"));
            assertFalse(primaryKeyColumns("recruitment_info").contains("id"));
            assertEquals(1, countRows("SELECT COUNT(*) FROM recruitment_info WHERE record_uuid = ?", recordUuid));
            assertEquals(generatedId, queryLong(
                    "SELECT id FROM recruitment_info WHERE record_uuid = ?", recordUuid));
        } finally {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM recruitment_info WHERE record_uuid = ?")) {
                statement.setString(1, recordUuid);
                statement.executeUpdate();
            }
        }
    }

    private Set<String> primaryKeyColumns(String table) throws SQLException {
        return queryStringSet(
                "SELECT column_name FROM information_schema.key_column_usage "
                        + "WHERE constraint_schema = ? AND table_name = ? AND constraint_name = 'PRIMARY'",
                SCHEMA, table);
    }

    private Set<String> indexColumns(String table, String indexName) throws SQLException {
        return queryStringSet(
                "SELECT column_name FROM information_schema.statistics "
                        + "WHERE table_schema = ? AND table_name = ? AND index_name = ? "
                        + "AND non_unique = 0",
                SCHEMA, table, indexName);
    }

    private Set<ForeignKey> queryForeignKeys() throws SQLException {
        Set<ForeignKey> result = new HashSet<>();
        String sql = "SELECT table_name, column_name, referenced_table_name, referenced_column_name "
                + "FROM information_schema.key_column_usage "
                + "WHERE constraint_schema = ? AND referenced_table_name IS NOT NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SCHEMA);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(new ForeignKey(
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4)));
                }
            }
        }
        return result;
    }

    private String columnDefault(String table, String column) throws SQLException {
        String sql = "SELECT column_default FROM information_schema.columns "
                + "WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SCHEMA);
            statement.setString(2, table);
            statement.setString(3, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "missing column " + table + "." + column);
                return resultSet.getString(1);
            }
        }
    }

    private String columnExtra(String table, String column) throws SQLException {
        String sql = "SELECT extra FROM information_schema.columns "
                + "WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SCHEMA);
            statement.setString(2, table);
            statement.setString(3, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "missing column " + table + "." + column);
                return resultSet.getString(1);
            }
        }
    }

    private long queryLong(String sql, String parameter) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getLong(1);
            }
        }
    }

    private int countRows(String sql, String parameter) throws SQLException {
        return (int) queryLong(sql, parameter);
    }

    private Set<String> queryStringSet(String sql, String... parameters) throws SQLException {
        Set<String> result = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setString(index + 1, parameters[index]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString(1));
                }
            }
        }
        return result;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record ForeignKey(String table, String column, String referencedTable,
                              String referencedColumn) {
    }
}
