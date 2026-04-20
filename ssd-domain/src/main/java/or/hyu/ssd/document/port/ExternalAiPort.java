package or.hyu.ssd.document.port;

import or.hyu.ssd.document.port.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.document.port.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.document.port.dto.ExternalAiHealthStatus;
import or.hyu.ssd.document.port.dto.ExternalEvaluationRequest;
import or.hyu.ssd.document.port.dto.ExternalEvaluationResponse;
import or.hyu.ssd.document.port.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.document.port.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.document.port.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.document.port.dto.ExternalSummarizationKeywordResponse;

public interface ExternalAiPort {
    ExternalAiHealthStatus health();

    ExternalEvaluationResponse evaluate(ExternalEvaluationRequest request);

    ExternalSummarizationBasicResponse summarizeBasic(ExternalSummarizationBasicRequest request);

    ExternalSummarizationKeywordResponse summarizeKeyword(ExternalSummarizationKeywordRequest request);

    ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request);
}
