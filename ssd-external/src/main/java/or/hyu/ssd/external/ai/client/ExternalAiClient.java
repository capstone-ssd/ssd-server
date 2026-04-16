package or.hyu.ssd.external.ai.client;

import or.hyu.ssd.external.ai.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.external.ai.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.external.ai.dto.ExternalEvaluationRequest;
import or.hyu.ssd.external.ai.dto.ExternalEvaluationResponse;
import or.hyu.ssd.external.ai.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.external.ai.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.external.ai.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.external.ai.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.external.ai.config.ExternalAiFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "externalAiClient",
        url = "${app.external-ai.base-url}",
        configuration = ExternalAiFeignConfig.class
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
}
