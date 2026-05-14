package or.hyu.ssd.api.document;

import or.hyu.ssd.api.bootstrap.SsdApplication;
import or.hyu.ssd.application.document.DocumentQueryFacade;
import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.document.application.support.DocumentSort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("performance")
@Testcontainers
@ActiveProfiles("local")
@SpringBootTest(classes = SsdApplication.class)
class DocumentSearchPerformanceIntegrationTest {

    private static final int DEFAULT_DOCUMENT_COUNT = 10_000;
    private static final int DEFAULT_WARMUPS = 10;
    private static final int DEFAULT_ITERATIONS = 50;
    private static final int BATCH_SIZE = 1_000;
    private static final String KEYWORD = "사업계획서";

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // given

        // when
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.show_sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.security.oauth2.client.registration.kakao.client-id", () -> "test-client-id");
        registry.add("app.external-ai.base-url", () -> "http://localhost:9999");
        registry.add("app.storage.s3.bucket", () -> "test-bucket");
        registry.add("app.storage.s3.region", () -> "ap-northeast-2");
        registry.add("app.storage.s3.access-key", () -> "test-access-key");
        registry.add("app.storage.s3.secret-key", () -> "test-secret-key");
        registry.add("sentry.dsn", () -> "");

        // then
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentQueryFacade documentQueryFacade;

    @Test
    @DisplayName("searchDocuments()는 대량 문서에서 제목 검색 성능 기준값을 출력한다")
    void searchDocuments_printsPerformanceBaseline() {
        // given
        int documentCount = Integer.getInteger("searchPerfDocs", DEFAULT_DOCUMENT_COUNT);
        int warmups = Integer.getInteger("searchPerfWarmups", DEFAULT_WARMUPS);
        int iterations = Integer.getInteger("searchPerfIterations", DEFAULT_ITERATIONS);
        Long ownerId = prepareDataset(documentCount);

        // when
        SearchPerformanceResult result = measure(
                () -> documentQueryFacade.searchDocuments(ownerId, KEYWORD, DocumentSort.MODIFIED),
                warmups,
                iterations
        );
        List<DocumentListItemResult> searchResults = documentQueryFacade.searchDocuments(ownerId, KEYWORD, DocumentSort.MODIFIED);

        // then
        assertThat(searchResults).isNotEmpty();
        assertThat(searchResults).allMatch(document -> document.title().contains(KEYWORD));
        assertThat(searchResults).allMatch(document -> document.id() <= documentCount);
        System.out.printf(
                "[DocumentSearchPerformance] docs=%d, warmups=%d, iterations=%d, keyword=%s, resultCount=%d, avg=%dms, p95=%dms, p99=%dms%n",
                documentCount,
                warmups,
                iterations,
                KEYWORD,
                searchResults.size(),
                result.avgMillis(),
                result.p95Millis(),
                result.p99Millis()
        );
    }

    private Long prepareDataset(int documentCount) {
        jdbcTemplate.execute("TRUNCATE TABLE documents, members RESTART IDENTITY CASCADE");
        Long ownerId = insertMember("owner@example.com");
        Long otherMemberId = insertMember("other@example.com");
        insertDocuments(ownerId, documentCount);
        insertOtherMemberDocument(otherMemberId);
        return ownerId;
    }

    private Long insertMember(String email) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO members (name, email, profile_image_url, role, created_at, updated_at)
                VALUES (?, ?, ?, ?, now(), now())
                RETURNING id
                """,
                Long.class,
                "테스터",
                email,
                "",
                "ROLE_AUTHOR"
        );
    }

    private void insertDocuments(Long memberId, int documentCount) {
        for (int start = 1; start <= documentCount; start += BATCH_SIZE) {
            int from = start;
            int size = Math.min(BATCH_SIZE, documentCount - start + 1);
            jdbcTemplate.batchUpdate(documentInsertSql(), new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    int sequence = from + index;
                    boolean matched = sequence % 100 == 0;
                    LocalDateTime timestamp = LocalDateTime.now().minusSeconds(documentCount - sequence);
                    ps.setString(1, matched ? "AI 사업계획서 " + sequence : "일반 문서 " + sequence);
                    ps.setString(2, "성능 테스트 본문 " + sequence);
                    ps.setBoolean(3, false);
                    ps.setString(4, "EVALUATION");
                    ps.setLong(5, memberId);
                    ps.setObject(6, timestamp);
                    ps.setObject(7, timestamp);
                    ps.setBoolean(8, false);
                    ps.setLong(9, 0L);
                }

                @Override
                public int getBatchSize() {
                    return size;
                }
            });
        }
    }

    private void insertOtherMemberDocument(Long memberId) {
        jdbcTemplate.update(
                documentInsertSql(),
                "AI 사업계획서 노출되면 안 되는 문서",
                "다른 회원 문서",
                false,
                "EVALUATION",
                memberId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                0L
        );
    }

    private String documentInsertSql() {
        return """
                INSERT INTO documents (
                    title, content, bookmark, purpose, member_id, created_at, updated_at,
                    external_ai_processed, version,
                    checklist_differentiation_is_clear,
                    checklist_differentiation_is_realistic,
                    checklist_target_specific_advantage,
                    checklist_entry_barrier_exists,
                    checklist_problem_is_clear,
                    checklist_problem_is_real,
                    checklist_target_and_context_are_specific,
                    checklist_existing_solution_has_limits,
                    checklist_market_definition_is_correct,
                    checklist_market_size_is_realistic,
                    checklist_willingness_to_pay_is_clear,
                    checklist_revenue_model_is_clear,
                    checklist_problem_founder_fit,
                    checklist_experience_alignment,
                    checklist_team_structure_is_clear,
                    checklist_capability_gap_plan_exists
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)
                """;
    }

    private SearchPerformanceResult measure(Runnable task, int warmups, int iterations) {
        for (int i = 0; i < warmups; i++) {
            task.run();
        }

        List<Long> elapsedNanos = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            task.run();
            elapsedNanos.add(System.nanoTime() - start);
        }
        Collections.sort(elapsedNanos);
        return new SearchPerformanceResult(
                averageMillis(elapsedNanos),
                percentileMillis(elapsedNanos, 95),
                percentileMillis(elapsedNanos, 99)
        );
    }

    private long averageMillis(List<Long> elapsedNanos) {
        return Math.round(elapsedNanos.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0) / TimeUnit.MILLISECONDS.toNanos(1));
    }

    private long percentileMillis(List<Long> elapsedNanos, int percentile) {
        int index = (int) Math.ceil(elapsedNanos.size() * (percentile / 100.0)) - 1;
        int safeIndex = Math.max(0, Math.min(index, elapsedNanos.size() - 1));
        return TimeUnit.NANOSECONDS.toMillis(elapsedNanos.get(safeIndex));
    }

    private record SearchPerformanceResult(
            long avgMillis,
            long p95Millis,
            long p99Millis
    ) {
    }
}
