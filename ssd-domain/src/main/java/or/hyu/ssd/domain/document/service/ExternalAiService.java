package or.hyu.ssd.domain.document.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.document.client.ExternalAiClient;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalAiService {

    private final ExternalAiClient externalAiClient;

    public ExternalEvaluationResponse evaluate(ExternalEvaluationRequest request) {
        return callExternalApi(() -> externalAiClient.evaluate(request), "POST /evaluate");
    }

    public ExternalSummarizationBasicResponse summarizeBasic(ExternalSummarizationBasicRequest request) {
        return callExternalApi(() -> externalAiClient.summarizeBasic(request), "POST /summarization/Basic");
    }

    public ExternalSummarizationKeywordResponse summarizeKeyword(ExternalSummarizationKeywordRequest request) {
        return callExternalApi(() -> externalAiClient.summarizeKeyword(request), "POST /summarization/Keyword");
    }

    public ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request) {
        return callExternalApi(() -> externalAiClient.checkNewText(request), "POST /check/new-text");
    }

    private <T> T callExternalApi(Supplier<T> supplier, String endpoint) {
        try {
            return supplier.get();
        } catch (FeignException e) {
            log.warn("[외부 AI 서버 호출 실패] endpoint={}, status={}, message={}", endpoint, e.status(), e.getMessage());
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_CALL_FAILED);
        } catch (Exception e) {
            log.error("[외부 AI 응답 처리 실패] endpoint={}", endpoint, e);
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_RESPONSE_INVALID);
        }
    }
}
