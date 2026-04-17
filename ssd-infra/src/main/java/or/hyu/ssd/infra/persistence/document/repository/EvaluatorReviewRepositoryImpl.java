package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.EvaluatorReview;
import or.hyu.ssd.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.member.mapper.MemberPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.EvaluatorReviewJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EvaluatorReviewRepositoryImpl implements EvaluatorReviewRepository {
    private final EvaluatorReviewJpaRepository evaluatorReviewJpaRepository;

    @Override
    public Optional<EvaluatorReview> findByDocumentAndReviewer(Document document, Member reviewer) {
        return evaluatorReviewJpaRepository.findByDocumentAndReviewer(
                DocumentPersistenceMapper.toDocumentRef(document),
                MemberPersistenceMapper.toRef(reviewer)
        ).map(DocumentPersistenceMapper::toDomain);
    }

    @Override
    public List<EvaluatorReview> findAllByDocument(Document document) {
        return evaluatorReviewJpaRepository.findAllByDocument(DocumentPersistenceMapper.toDocumentRef(document)).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<EvaluatorReview> findAllByDocumentOrderByUpdatedAtDesc(Document document) {
        return evaluatorReviewJpaRepository.findAllByDocumentOrderByUpdatedAtDesc(DocumentPersistenceMapper.toDocumentRef(document)).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDocumentAndReviewer(Document document, Member reviewer) {
        return evaluatorReviewJpaRepository.existsByDocumentAndReviewer(
                DocumentPersistenceMapper.toDocumentRef(document),
                MemberPersistenceMapper.toRef(reviewer)
        );
    }

    @Override
    public EvaluatorReview save(EvaluatorReview review) {
        return DocumentPersistenceMapper.toDomain(evaluatorReviewJpaRepository.save(DocumentPersistenceMapper.toJpa(review)));
    }

    @Override
    public void delete(EvaluatorReview review) {
        if (review.getId() != null) {
            evaluatorReviewJpaRepository.deleteById(review.getId());
            return;
        }
        evaluatorReviewJpaRepository.delete(DocumentPersistenceMapper.toJpa(review));
    }

    @Override
    public void deleteAllByDocument(Document document) {
        evaluatorReviewJpaRepository.deleteAllByDocument(DocumentPersistenceMapper.toDocumentRef(document));
    }
}
