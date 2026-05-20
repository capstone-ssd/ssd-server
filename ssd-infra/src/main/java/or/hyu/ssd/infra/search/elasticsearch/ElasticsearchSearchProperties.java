package or.hyu.ssd.infra.search.elasticsearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.search.elasticsearch")
public class ElasticsearchSearchProperties {

    private boolean enabled = false;
    private String baseUrl = "http://localhost:9200";
    private String indexName = "ssd-dev-documents";
    private Duration requestTimeout = Duration.ofSeconds(3);
}
