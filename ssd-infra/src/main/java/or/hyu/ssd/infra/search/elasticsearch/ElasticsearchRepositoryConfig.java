package or.hyu.ssd.infra.search.elasticsearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(prefix = "app.search.elasticsearch", name = "enabled", havingValue = "true")
@EnableElasticsearchRepositories(basePackages = "or.hyu.ssd.infra.search.elasticsearch")
public class ElasticsearchRepositoryConfig {
}
