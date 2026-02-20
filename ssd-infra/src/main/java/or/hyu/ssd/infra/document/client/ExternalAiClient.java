package or.hyu.ssd.infra.document.client;

import feign.Request;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
        public Request.Options externalAiRequestOptions(
                @Value("${feign.client.config.externalAiClient.connectTimeout:5000}") int connectTimeout,
                @Value("${feign.client.config.externalAiClient.readTimeout:300000}") int readTimeout
        ) {
            log.info("[ExternalAiClient Timeout] connectTimeout={}ms, readTimeout={}ms", connectTimeout, readTimeout);
            return new Request.Options(connectTimeout, readTimeout);
        }
    }
}
