package or.hyu.ssd.infra.search.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.repository.DocumentSearchRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchDocumentSearchRepository implements DocumentSearchRepository {

    private static final int SEARCH_SIZE = 100;

    private final ElasticsearchDocumentRepository elasticsearchDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchSearchProperties properties;

    @Override
    public List<Document> searchDocuments(Long memberId, String keyword, Sort sort) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(searchQuery(memberId, keyword, false))
                .withMaxResults(SEARCH_SIZE)
                .withSort(nativeSort(sort))
                .build();
        return search(query).stream()
                .map(SearchHit::getContent)
                .map(ElasticsearchDocument::toDomain)
                .toList();
    }

    @Override
    public List<Document> searchDocumentsByTitlePrefix(Long memberId, String keyword, Sort sort) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(searchQuery(memberId, keyword, true))
                .withMaxResults(SEARCH_SIZE)
                .withSort(nativeSort(sort))
                .build();
        return search(query).stream()
                .map(SearchHit::getContent)
                .map(ElasticsearchDocument::toDomain)
                .toList();
    }

    @Override
    public List<DocumentSearchSuggestionResult> suggestSearchKeywords(Long memberId, String keyword, int limit, double threshold) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(suggestionQuery(memberId, keyword))
                .withMaxResults(Math.max(limit * 3, limit))
                .withSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("_score")))
                .withSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("updatedAt")))
                .build();

        Map<String, Double> candidates = new LinkedHashMap<>();
        for (SearchHit<ElasticsearchDocument> hit : search(query)) {
            double score = hit.getScore();
            ElasticsearchDocument document = hit.getContent();
            mergeCandidate(candidates, document.getTitle(), score);
            if (document.getKeywords() != null) {
                for (String value : document.getKeywords().split(",")) {
                    mergeCandidate(candidates, value.trim(), score);
                }
            }
        }

        return candidates.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(entry -> DocumentSearchSuggestionResult.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public void index(Document document) {
        if (document == null || document.getId() == null) {
            return;
        }
        elasticsearchDocumentRepository.save(ElasticsearchDocument.from(document));
    }

    @Override
    public void delete(Long documentId) {
        if (documentId == null) {
            return;
        }
        elasticsearchDocumentRepository.deleteById(documentId);
    }

    private List<SearchHit<ElasticsearchDocument>> search(NativeQuery query) {
        SearchHits<ElasticsearchDocument> hits = elasticsearchOperations.search(
                query,
                ElasticsearchDocument.class,
                IndexCoordinates.of(properties.getIndexName())
        );
        return hits.getSearchHits();
    }

    private Query searchQuery(Long memberId, String keyword, boolean prefix) {
        Query textQuery = prefix
                ? Query.of(query -> query.matchPhrasePrefix(match -> match.field("title").query(keyword)))
                : Query.of(query -> query.multiMatch(match -> match
                .query(keyword)
                .fields("title^3", "keywords")
        ));
        return boolQuery(memberId, textQuery);
    }

    private Query suggestionQuery(Long memberId, String keyword) {
        Query textQuery = Query.of(query -> query.multiMatch(match -> match
                .query(keyword)
                .fields("title^3", "keywords")
        ));
        return boolQuery(memberId, textQuery);
    }

    private Query boolQuery(Long memberId, Query textQuery) {
        return Query.of(query -> query.bool(bool -> bool
                .filter(filter -> filter.term(term -> term.field("memberId").value(memberId)))
                .filter(filter -> filter.term(term -> term.field("deleted").value(false)))
                .must(textQuery)
        ));
    }

    private Sort nativeSort(Sort sort) {
        Sort result = Sort.by(Sort.Order.desc("_score"));
        if (sort == null) {
            return result;
        }
        for (Sort.Order order : sort) {
            String property = switch (order.getProperty()) {
                case "title" -> "title.keyword";
                case "createdAt", "updatedAt" -> order.getProperty();
                default -> null;
            };
            if (property != null) {
                result = result.and(Sort.by(new Sort.Order(order.getDirection(), property)));
            }
        }
        return result;
    }

    private void mergeCandidate(Map<String, Double> candidates, String keyword, double score) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        candidates.merge(keyword, score, Math::max);
    }
}
