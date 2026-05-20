package or.hyu.ssd.infra.search.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) {
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(ElasticsearchDocument.class);
            if (!indexOperations.exists()) {
                indexOperations.createWithMapping();
            }
        } catch (RuntimeException e) {
            log.warn("Elasticsearch index 초기화에 실패했습니다. 검색은 PostgreSQL fallback으로 동작합니다.", e);
        }
    }
}
