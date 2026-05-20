package or.hyu.ssd.infra.search.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchHttpClient elasticsearchHttpClient;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!elasticsearchHttpClient.existsIndex()) {
                elasticsearchHttpClient.createIndex(indexMapping());
            }
        } catch (RuntimeException e) {
            log.warn("Elasticsearch index 초기화에 실패했습니다. 검색은 PostgreSQL fallback으로 동작합니다.", e);
        }
    }

    private String indexMapping() {
        return """
                {
                  "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 0,
                    "analysis": {
                      "analyzer": {
                        "ssd_autocomplete": {
                          "tokenizer": "ssd_autocomplete_tokenizer",
                          "filter": ["lowercase"]
                        }
                      },
                      "tokenizer": {
                        "ssd_autocomplete_tokenizer": {
                          "type": "edge_ngram",
                          "min_gram": 1,
                          "max_gram": 20,
                          "token_chars": ["letter", "digit"]
                        }
                      }
                    }
                  },
                  "mappings": {
                    "properties": {
                      "documentId": { "type": "long" },
                      "memberId": { "type": "long" },
                      "folderId": { "type": "long" },
                      "title": {
                        "type": "text",
                        "fields": {
                          "keyword": { "type": "keyword" },
                          "autocomplete": {
                            "type": "text",
                            "analyzer": "ssd_autocomplete",
                            "search_analyzer": "standard"
                          }
                        }
                      },
                      "keywords": { "type": "text" },
                      "purpose": { "type": "keyword" },
                      "bookmark": { "type": "boolean" },
                      "deleted": { "type": "boolean" },
                      "createdAt": { "type": "date", "format": "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss" },
                      "updatedAt": { "type": "date", "format": "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss" }
                    }
                  }
                }
                """;
    }
}
