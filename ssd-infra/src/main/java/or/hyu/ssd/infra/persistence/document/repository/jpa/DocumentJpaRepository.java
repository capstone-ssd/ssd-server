package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.repository.projection.DocumentSearchSuggestionProjection;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, Long> {
    List<DocumentJpaEntity> findAllByMember_Id(Long memberId, Sort sort);

    @EntityGraph(attributePaths = {"folder", "member"})
    List<DocumentJpaEntity> findAllByMember_IdAndTitleContaining(Long memberId, String keyword, Sort sort);

    @EntityGraph(attributePaths = {"folder", "member"})
    List<DocumentJpaEntity> findAllByMember_IdAndTitleStartingWith(Long memberId, String keyword, Sort sort);

    @Query(value = """
            SELECT candidate.keyword AS keyword,
                   MAX(candidate.score) AS score
            FROM (
                SELECT d.title AS keyword,
                       similarity(d.title, :keyword) AS score
                FROM documents d
                WHERE d.member_id = :memberId
                  AND d.title IS NOT NULL
                  AND d.title % :keyword

                UNION ALL

                SELECT trimmed.keyword AS keyword,
                       similarity(trimmed.keyword, :keyword) AS score
                FROM documents d
                CROSS JOIN LATERAL regexp_split_to_table(coalesce(d.keywords, ''), ',') AS raw(keyword)
                CROSS JOIN LATERAL (
                    SELECT trim(raw.keyword) AS keyword
                ) trimmed
                WHERE d.member_id = :memberId
                  AND d.keywords IS NOT NULL
                  AND d.keywords % :keyword
                  AND trimmed.keyword <> ''
            ) candidate
            WHERE candidate.score >= :threshold
            GROUP BY candidate.keyword
            ORDER BY score DESC, keyword ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentSearchSuggestionProjection> findSearchSuggestions(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("threshold") double threshold
    );

    List<DocumentJpaEntity> findAllByMember_IdAndFolder_Id(Long memberId, Long folderId, Sort sort);

    List<DocumentJpaEntity> findAllByMember_IdAndFolderIsNull(Long memberId, Sort sort);

    List<DocumentJpaEntity> findAllByFolder_Id(Long folderId);
}
