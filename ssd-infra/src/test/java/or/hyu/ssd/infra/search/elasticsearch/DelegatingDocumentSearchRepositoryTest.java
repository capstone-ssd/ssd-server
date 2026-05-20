package or.hyu.ssd.infra.search.elasticsearch;

import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.infra.persistence.document.repository.PostgresDocumentSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegatingDocumentSearchRepositoryTest {

    @Mock
    private PostgresDocumentSearchRepository postgresDocumentSearchRepository;
    @Mock
    private ElasticsearchDocumentSearchRepository elasticsearchDocumentSearchRepository;

    @Test
    @DisplayName("searchDocuments()는 Elasticsearch가 없으면 PostgreSQL 검색을 사용한다")
    void searchDocuments_usesPostgresWhenElasticsearchIsUnavailable() {
        // given
        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));
        Document document = Document.builder().id(1L).title("사업계획서").content("").build();
        DelegatingDocumentSearchRepository repository = new DelegatingDocumentSearchRepository(
                postgresDocumentSearchRepository,
                provider(null)
        );
        when(postgresDocumentSearchRepository.searchDocuments(1L, "사업", sort)).thenReturn(List.of(document));

        // when
        List<Document> results = repository.searchDocuments(1L, "사업", sort);

        // then
        assertThat(results).containsExactly(document);
        verify(postgresDocumentSearchRepository).searchDocuments(1L, "사업", sort);
    }

    @Test
    @DisplayName("searchDocuments()는 Elasticsearch 실패 시 PostgreSQL 검색으로 fallback한다")
    void searchDocuments_fallsBackToPostgresWhenElasticsearchFails() {
        // given
        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));
        Document document = Document.builder().id(1L).title("사업계획서").content("").build();
        DelegatingDocumentSearchRepository repository = new DelegatingDocumentSearchRepository(
                postgresDocumentSearchRepository,
                provider(elasticsearchDocumentSearchRepository)
        );
        when(elasticsearchDocumentSearchRepository.searchDocuments(1L, "사업", sort))
                .thenThrow(new IllegalStateException("ES down"));
        when(postgresDocumentSearchRepository.searchDocuments(1L, "사업", sort)).thenReturn(List.of(document));

        // when
        List<Document> results = repository.searchDocuments(1L, "사업", sort);

        // then
        assertThat(results).containsExactly(document);
        verify(elasticsearchDocumentSearchRepository).searchDocuments(1L, "사업", sort);
        verify(postgresDocumentSearchRepository).searchDocuments(1L, "사업", sort);
    }

    private ObjectProvider<ElasticsearchDocumentSearchRepository> provider(ElasticsearchDocumentSearchRepository repository) {
        return new ObjectProvider<>() {
            @Override
            public ElasticsearchDocumentSearchRepository getObject(Object... args) {
                return repository;
            }

            @Override
            public ElasticsearchDocumentSearchRepository getIfAvailable() {
                return repository;
            }

            @Override
            public ElasticsearchDocumentSearchRepository getIfUnique() {
                return repository;
            }

            @Override
            public ElasticsearchDocumentSearchRepository getObject() {
                return repository;
            }

            @Override
            public Iterator<ElasticsearchDocumentSearchRepository> iterator() {
                return repository == null ? List.<ElasticsearchDocumentSearchRepository>of().iterator() : List.of(repository).iterator();
            }

            @Override
            public Stream<ElasticsearchDocumentSearchRepository> stream() {
                return repository == null ? Stream.empty() : Stream.of(repository);
            }

            @Override
            public Stream<ElasticsearchDocumentSearchRepository> orderedStream() {
                return stream();
            }
        };
    }
}
