package or.hyu.ssd.infra.search.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.repository.DocumentSearchRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchDocumentSearchRepository implements DocumentSearchRepository {

    private static final int SEARCH_SIZE = 100;

    private final ElasticsearchHttpClient elasticsearchHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<Document> searchDocuments(Long memberId, String keyword, Sort sort) {
        JsonNode response = elasticsearchHttpClient.search(searchQuery(memberId, keyword, sort, false));
        return parseDocuments(response);
    }

    @Override
    public List<Document> searchDocumentsByTitlePrefix(Long memberId, String keyword, Sort sort) {
        JsonNode response = elasticsearchHttpClient.search(searchQuery(memberId, keyword, sort, true));
        return parseDocuments(response);
    }

    @Override
    public List<DocumentSearchSuggestionResult> suggestSearchKeywords(Long memberId, String keyword, int limit, double threshold) {
        JsonNode response = elasticsearchHttpClient.search(suggestionQuery(memberId, keyword, limit));
        Map<String, Double> candidates = new LinkedHashMap<>();
        for (JsonNode hit : response.path("hits").path("hits")) {
            double score = hit.path("_score").asDouble(0.0);
            JsonNode source = hit.path("_source");
            mergeCandidate(candidates, source.path("title").asText(null), score);
            String keywords = source.path("keywords").asText(null);
            if (keywords != null) {
                for (String value : keywords.split(",")) {
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
        elasticsearchHttpClient.indexDocument(ElasticsearchDocument.from(document));
    }

    @Override
    public void delete(Long documentId) {
        if (documentId == null) {
            return;
        }
        elasticsearchHttpClient.deleteDocument(documentId);
    }

    private String searchQuery(Long memberId, String keyword, Sort sort, boolean prefix) {
        String matchClause = prefix
                ? """
                  { "match_phrase_prefix": { "title.autocomplete": { "query": %s } } }
                  """.formatted(toJson(keyword))
                : """
                  {
                    "multi_match": {
                      "query": %s,
                      "fields": ["title^3", "keywords"],
                      "type": "best_fields"
                    }
                  }
                  """.formatted(toJson(keyword));
        return """
                {
                  "size": %d,
                  "query": {
                    "bool": {
                      "filter": [
                        { "term": { "memberId": %d } },
                        { "term": { "deleted": false } }
                      ],
                      "must": [ %s ]
                    }
                  },
                  "sort": %s
                }
                """.formatted(SEARCH_SIZE, memberId, matchClause, sortJson(sort));
    }

    private String suggestionQuery(Long memberId, String keyword, int limit) {
        return """
                {
                  "size": %d,
                  "query": {
                    "bool": {
                      "filter": [
                        { "term": { "memberId": %d } },
                        { "term": { "deleted": false } }
                      ],
                      "must": [
                        {
                          "multi_match": {
                            "query": %s,
                            "fields": ["title.autocomplete^3", "title^2", "keywords"],
                            "type": "best_fields"
                          }
                        }
                      ]
                    }
                  },
                  "sort": ["_score", { "updatedAt": { "order": "desc" } }]
                }
                """.formatted(Math.max(limit * 3, limit), memberId, toJson(keyword));
    }

    private String sortJson(Sort sort) {
        List<String> sortFields = new ArrayList<>();
        sortFields.add("\"_score\"");
        if (sort != null) {
            for (Sort.Order order : sort) {
                String property = switch (order.getProperty()) {
                    case "title" -> "title.keyword";
                    case "createdAt", "updatedAt" -> order.getProperty();
                    default -> null;
                };
                if (property != null) {
                    sortFields.add("{ \"" + property + "\": { \"order\": \"" + order.getDirection().name().toLowerCase() + "\" } }");
                }
            }
        }
        return "[" + String.join(", ", sortFields) + "]";
    }

    private List<Document> parseDocuments(JsonNode response) {
        List<Document> documents = new ArrayList<>();
        for (JsonNode hit : response.path("hits").path("hits")) {
            documents.add(objectMapper.convertValue(hit.path("_source"), ElasticsearchDocument.class).toDomain());
        }
        return documents;
    }

    private void mergeCandidate(Map<String, Double> candidates, String keyword, double score) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        candidates.merge(keyword, score, Math::max);
    }

    private String toJson(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("검색어 직렬화에 실패했습니다", e);
        }
    }
}
