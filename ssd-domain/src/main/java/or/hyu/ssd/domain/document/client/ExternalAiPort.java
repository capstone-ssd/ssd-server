package or.hyu.ssd.domain.document.client;

import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;

public interface ExternalAiPort {
    ExternalEvaluationResponse evaluate(ExternalEvaluationRequest request);

    ExternalSummarizationBasicResponse summarizeBasic(ExternalSummarizationBasicRequest request);

    ExternalSummarizationKeywordResponse summarizeKeyword(ExternalSummarizationKeywordRequest request);

    ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request);
}
