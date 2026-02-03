package or.hyu.ssd.infra.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.domain.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.infra.document.repository.jpa.EvaluatorReviewJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EvaluatorReviewRepositoryImpl implements EvaluatorReviewRepository {
    private final EvaluatorReviewJpaRepository evaluatorReviewJpaRepository;

    @Override
    public Optional<EvaluatorReview> findByDocumentAndReviewer(Document document, Member reviewer) {
        return evaluatorReviewJpaRepository.findByDocumentAndReviewer(document, reviewer);
    }

    @Override
    public List<EvaluatorReview> findAllByDocument(Document document) {
        return evaluatorReviewJpaRepository.findAllByDocument(document);
    }

    @Override
    public boolean existsByDocumentAndReviewer(Document document, Member reviewer) {
        return evaluatorReviewJpaRepository.existsByDocumentAndReviewer(document, reviewer);
    }

    @Override
    public EvaluatorReview save(EvaluatorReview review) {
        return evaluatorReviewJpaRepository.save(review);
    }
}
