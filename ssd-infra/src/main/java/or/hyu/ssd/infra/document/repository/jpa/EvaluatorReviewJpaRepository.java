package or.hyu.ssd.infra.document.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluatorReviewJpaRepository extends JpaRepository<EvaluatorReview, Long> {
    Optional<EvaluatorReview> findByDocumentAndReviewer(Document document, Member reviewer);

    List<EvaluatorReview> findAllByDocument(Document document);

    List<EvaluatorReview> findAllByDocumentOrderByUpdatedAtDesc(Document document);

    boolean existsByDocumentAndReviewer(Document document, Member reviewer);

    void deleteAllByDocument(Document document);
}
