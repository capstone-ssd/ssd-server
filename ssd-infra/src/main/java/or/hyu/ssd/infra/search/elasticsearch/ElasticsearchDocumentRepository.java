package or.hyu.ssd.infra.search.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticsearchDocumentRepository extends ElasticsearchRepository<ElasticsearchDocument, Long> {
}
