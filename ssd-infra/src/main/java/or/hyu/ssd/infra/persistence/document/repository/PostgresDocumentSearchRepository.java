package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.repository.DocumentSearchRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.DocumentJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostgresDocumentSearchRepository implements DocumentSearchRepository {

    private final DocumentJpaRepository documentJpaRepository;

    @Override
    public List<Document> searchDocuments(Long memberId, String keyword, Sort sort) {
        return documentJpaRepository.findAllByMember_IdAndTitleContaining(memberId, keyword, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Document> searchDocumentsByTitlePrefix(Long memberId, String keyword, Sort sort) {
        return documentJpaRepository.findAllByMember_IdAndTitleStartingWith(memberId, keyword, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<DocumentSearchSuggestionResult> suggestSearchKeywords(Long memberId, String keyword, int limit, double threshold) {
        return documentJpaRepository.findSearchSuggestions(memberId, keyword, limit, threshold).stream()
                .map(projection -> DocumentSearchSuggestionResult.of(
                        projection.getKeyword(),
                        projection.getScore() == null ? 0.0 : projection.getScore()
                ))
                .toList();
    }

    @Override
    public void index(Document document) {
        // PostgreSQL 검색은 원본 테이블을 직접 조회하므로 별도 색인 작업이 필요하지 않다.
    }

    @Override
    public void delete(Long documentId) {
        // PostgreSQL 검색은 원본 테이블 삭제 결과를 그대로 사용한다.
    }
}
