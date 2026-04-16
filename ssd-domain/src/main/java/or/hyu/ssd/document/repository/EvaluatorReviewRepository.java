package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.EvaluatorReview;
import or.hyu.ssd.member.domain.entity.Member;

import java.util.List;
import java.util.Optional;

public interface EvaluatorReviewRepository {
    Optional<EvaluatorReview> findByDocumentAndReviewer(Document document, Member reviewer);

    List<EvaluatorReview> findAllByDocument(Document document);

    List<EvaluatorReview> findAllByDocumentOrderByUpdatedAtDesc(Document document);

    boolean existsByDocumentAndReviewer(Document document, Member reviewer);

    EvaluatorReview save(EvaluatorReview review);

    void delete(EvaluatorReview review);

    void deleteAllByDocument(Document document);
}
