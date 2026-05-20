package or.hyu.ssd.infra.search.elasticsearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.search.elasticsearch")
public class ElasticsearchSearchProperties {

    private boolean enabled = false;
    private String indexName = "ssd-dev-documents";
}
