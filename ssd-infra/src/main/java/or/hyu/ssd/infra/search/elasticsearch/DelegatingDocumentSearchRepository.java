package or.hyu.ssd.infra.search.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.repository.DocumentSearchRepository;
import or.hyu.ssd.infra.persistence.document.repository.PostgresDocumentSearchRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class DelegatingDocumentSearchRepository implements DocumentSearchRepository {

    private final PostgresDocumentSearchRepository postgresDocumentSearchRepository;
    private final ObjectProvider<ElasticsearchDocumentSearchRepository> elasticsearchDocumentSearchRepositoryProvider;

    @Override
    public List<Document> searchDocuments(Long memberId, String keyword, Sort sort) {
        ElasticsearchDocumentSearchRepository elasticsearch = elasticsearchDocumentSearchRepositoryProvider.getIfAvailable();
        if (elasticsearch == null) {
            return postgresDocumentSearchRepository.searchDocuments(memberId, keyword, sort);
        }
        try {
            return elasticsearch.searchDocuments(memberId, keyword, sort);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch 문서 검색에 실패하여 PostgreSQL 검색으로 fallback합니다. memberId={}, keyword={}", memberId, keyword, e);
            return postgresDocumentSearchRepository.searchDocuments(memberId, keyword, sort);
        }
    }

    @Override
    public List<Document> searchDocumentsByTitlePrefix(Long memberId, String keyword, Sort sort) {
        ElasticsearchDocumentSearchRepository elasticsearch = elasticsearchDocumentSearchRepositoryProvider.getIfAvailable();
        if (elasticsearch == null) {
            return postgresDocumentSearchRepository.searchDocumentsByTitlePrefix(memberId, keyword, sort);
        }
        try {
            return elasticsearch.searchDocumentsByTitlePrefix(memberId, keyword, sort);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch 문서 prefix 검색에 실패하여 PostgreSQL 검색으로 fallback합니다. memberId={}, keyword={}", memberId, keyword, e);
            return postgresDocumentSearchRepository.searchDocumentsByTitlePrefix(memberId, keyword, sort);
        }
    }

    @Override
    public List<DocumentSearchSuggestionResult> suggestSearchKeywords(Long memberId, String keyword, int limit, double threshold) {
        ElasticsearchDocumentSearchRepository elasticsearch = elasticsearchDocumentSearchRepositoryProvider.getIfAvailable();
        if (elasticsearch == null) {
            return postgresDocumentSearchRepository.suggestSearchKeywords(memberId, keyword, limit, threshold);
        }
        try {
            return elasticsearch.suggestSearchKeywords(memberId, keyword, limit, threshold);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch 검색어 추천에 실패하여 PostgreSQL 검색으로 fallback합니다. memberId={}, keyword={}", memberId, keyword, e);
            return postgresDocumentSearchRepository.suggestSearchKeywords(memberId, keyword, limit, threshold);
        }
    }

    @Override
    public void index(Document document) {
        ElasticsearchDocumentSearchRepository elasticsearch = elasticsearchDocumentSearchRepositoryProvider.getIfAvailable();
        if (elasticsearch == null) {
            return;
        }
        try {
            elasticsearch.index(document);
        } catch (RuntimeException e) {
            Long documentId = document == null ? null : document.getId();
            log.warn("Elasticsearch 문서 색인에 실패했습니다. documentId={}", documentId, e);
        }
    }

    @Override
    public void delete(Long documentId) {
        ElasticsearchDocumentSearchRepository elasticsearch = elasticsearchDocumentSearchRepositoryProvider.getIfAvailable();
        if (elasticsearch == null) {
            return;
        }
        try {
            elasticsearch.delete(documentId);
        } catch (RuntimeException e) {
            log.warn("Elasticsearch 문서 색인 삭제에 실패했습니다. documentId={}", documentId, e);
        }
    }
}
