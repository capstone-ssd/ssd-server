package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.EvaluatorReviewJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluatorReviewJpaRepository extends JpaRepository<EvaluatorReviewJpaEntity, Long> {
    Optional<EvaluatorReviewJpaEntity> findByDocumentAndReviewer(DocumentJpaEntity document, MemberJpaEntity reviewer);

    List<EvaluatorReviewJpaEntity> findAllByDocument(DocumentJpaEntity document);

    List<EvaluatorReviewJpaEntity> findAllByDocumentOrderByUpdatedAtDesc(DocumentJpaEntity document);

    boolean existsByDocumentAndReviewer(DocumentJpaEntity document, MemberJpaEntity reviewer);

    void deleteAllByDocument(DocumentJpaEntity document);
}
