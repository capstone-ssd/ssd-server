package or.hyu.ssd.infra.search.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.search.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchHttpClient {

    private final ElasticsearchSearchProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public boolean existsIndex() {
        HttpRequest request = requestBuilder(indexUri())
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        int statusCode = send(request).statusCode();
        return statusCode >= 200 && statusCode < 300;
    }

    public void createIndex(String mappingJson) {
        HttpRequest request = requestBuilder(indexUri())
                .PUT(HttpRequest.BodyPublishers.ofString(mappingJson))
                .header("Content-Type", "application/json")
                .build();
        ensureSuccess(send(request));
    }

    public void indexDocument(ElasticsearchDocument document) {
        try {
            String body = objectMapper.writeValueAsString(document);
            HttpRequest request = requestBuilder(indexUri() + "/_doc/" + document.documentId())
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .build();
            ensureSuccess(send(request));
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch 문서 직렬화에 실패했습니다", e);
        }
    }

    public void deleteDocument(Long documentId) {
        HttpRequest request = requestBuilder(indexUri() + "/_doc/" + documentId)
                .DELETE()
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() == 404) {
            return;
        }
        ensureSuccess(response);
    }

    public JsonNode search(String queryJson) {
        HttpRequest request = requestBuilder(indexUri() + "/_search")
                .POST(HttpRequest.BodyPublishers.ofString(queryJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = send(request);
        ensureSuccess(response);
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch 검색 응답 파싱에 실패했습니다", e);
        }
    }

    private HttpRequest.Builder requestBuilder(String uri) {
        return HttpRequest.newBuilder(URI.create(uri))
                .timeout(properties.getRequestTimeout());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("Elasticsearch 호출에 실패했습니다", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Elasticsearch 호출이 중단되었습니다", e);
        }
    }

    private void ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Elasticsearch 호출에 실패했습니다. status=" + response.statusCode() + ", body=" + response.body());
        }
    }

    private String indexUri() {
        return normalizeBaseUrl() + "/" + properties.getIndexName();
    }

    private String normalizeBaseUrl() {
        String baseUrl = properties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return "http://localhost:9200";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
