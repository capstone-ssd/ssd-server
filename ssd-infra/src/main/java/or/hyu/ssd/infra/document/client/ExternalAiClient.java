package or.hyu.ssd.infra.document.client;

import feign.Request;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.document.port.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.document.port.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.document.port.dto.ExternalEvaluationRequest;
import or.hyu.ssd.document.port.dto.ExternalEvaluationResponse;
import or.hyu.ssd.document.port.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.document.port.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.document.port.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.document.port.dto.ExternalSummarizationKeywordResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Duration;

@FeignClient(
        name = "externalAiClient",
        url = "${app.external-ai.base-url}",
        configuration = ExternalAiClient.ExternalAiClientConfig.class
)
public interface ExternalAiClient {

    @PostMapping("/evaluate")
    ExternalEvaluationResponse evaluate(@RequestBody ExternalEvaluationRequest request);

    @PostMapping("/summarization/Basic")
    ExternalSummarizationBasicResponse summarizeBasic(@RequestBody ExternalSummarizationBasicRequest request);

    @PostMapping("/summarization/Keyword")
    ExternalSummarizationKeywordResponse summarizeKeyword(@RequestBody ExternalSummarizationKeywordRequest request);

    @PostMapping("/check/new-text")
    ExternalCheckNewTextResponse checkNewText(@RequestBody ExternalCheckNewTextRequest request);

    @Configuration
    @Slf4j
    class ExternalAiClientConfig {
        @Bean
        public Request.Options externalAiRequestOptions() {
            log.info("[ExternalAiClient Timeout] timeout disabled");
            return new Request.Options(Duration.ZERO, Duration.ZERO, true);
        }
    }
}
