package or.hyu.ssd.infra.adapter.document;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.document.port.dto.ExternalAiHealthStatus;
import or.hyu.ssd.external.ai.client.ExternalAiClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ExternalAiPortAdapterTest {

    @Mock
    private ExternalAiClient externalAiClient;

    @InjectMocks
    private ExternalAiPortAdapter externalAiPortAdapter;

    @Test
    @DisplayName("health()는 외부 AI 서버가 정상 응답하면 UP을 반환한다")
    void health_returnsUpWhenExternalAiServerResponds() {
        // given
        doNothing().when(externalAiClient).health();

        // when
        ExternalAiHealthStatus response = externalAiPortAdapter.health();

        // then
        assertThat(response.available()).isTrue();
        assertThat(response.message()).isEqualTo("외부 AI 서버가 정상 응답했습니다.");
    }

    @Test
    @DisplayName("health()는 외부 AI 서버가 비정상 응답하면 DOWN을 반환한다")
    void health_returnsDownWhenExternalAiServerRespondsWithError() {
        // given
        doThrow(serviceUnavailable()).when(externalAiClient).health();

        // when
        ExternalAiHealthStatus response = externalAiPortAdapter.health();

        // then
        assertThat(response.available()).isFalse();
        assertThat(response.message()).isEqualTo("외부 AI 서버가 비정상 응답을 반환했습니다. status=503");
    }

    private FeignException serviceUnavailable() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/health",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );
        Response response = Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("GET /health", response);
    }
}
