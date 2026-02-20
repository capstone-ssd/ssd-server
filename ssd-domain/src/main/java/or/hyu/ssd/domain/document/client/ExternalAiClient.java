package or.hyu.ssd.domain.document.client;

import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "externalAiClient",
        url = "${app.external-ai.base-url}"
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
