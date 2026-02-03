package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface EvaluatorReviewRepository {
    Optional<EvaluatorReview> findByDocumentAndReviewer(Document document, Member reviewer);

    List<EvaluatorReview> findAllByDocument(Document document);

    boolean existsByDocumentAndReviewer(Document document, Member reviewer);

    EvaluatorReview save(EvaluatorReview review);
}
