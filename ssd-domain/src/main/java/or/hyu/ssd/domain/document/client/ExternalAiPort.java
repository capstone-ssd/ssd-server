package or.hyu.ssd.domain.document.client;

import or.hyu.ssd.domain.document.client.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationKeywordResponse;

public interface ExternalAiPort {
    ExternalEvaluationResponse evaluate(ExternalEvaluationRequest request);

    ExternalSummarizationBasicResponse summarizeBasic(ExternalSummarizationBasicRequest request);

    ExternalSummarizationKeywordResponse summarizeKeyword(ExternalSummarizationKeywordRequest request);

    ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request);
}
