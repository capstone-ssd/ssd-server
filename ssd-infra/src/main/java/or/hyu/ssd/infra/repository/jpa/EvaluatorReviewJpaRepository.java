package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluatorReviewJpaRepository extends JpaRepository<EvaluatorReview, Long> {
    Optional<EvaluatorReview> findByDocumentAndReviewer(Document document, Member reviewer);

    List<EvaluatorReview> findAllByDocument(Document document);

    boolean existsByDocumentAndReviewer(Document document, Member reviewer);
}
