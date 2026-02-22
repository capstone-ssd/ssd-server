package or.hyu.ssd.infra.document.client;

import feign.FeignException;
import feign.codec.DecodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.document.client.ExternalAiPort;
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
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalAiPortAdapter implements ExternalAiPort {

    private static final int MAX_LOG_BODY_LENGTH = 200;

    private final ExternalAiClient externalAiClient;

    @Override
    public ExternalEvaluationResponse evaluate(ExternalEvaluationRequest request) {
        return callExternalApi(() -> externalAiClient.evaluate(request), "POST /evaluate");
    }

    @Override
    public ExternalSummarizationBasicResponse summarizeBasic(ExternalSummarizationBasicRequest request) {
        return callExternalApi(() -> externalAiClient.summarizeBasic(request), "POST /summarization/Basic");
    }

    @Override
    public ExternalSummarizationKeywordResponse summarizeKeyword(ExternalSummarizationKeywordRequest request) {
        return callExternalApi(() -> externalAiClient.summarizeKeyword(request), "POST /summarization/Keyword");
    }

    @Override
    public ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request) {
        return callExternalApi(() -> externalAiClient.checkNewText(request), "POST /check/new-text");
    }

    private <T> T callExternalApi(Supplier<T> supplier, String endpoint) {
        try {
            return supplier.get();
        } catch (DecodeException e) {
            log.error("[외부 AI 응답 처리 실패] endpoint={}, message={}", endpoint, sanitize(e.getMessage(), MAX_LOG_BODY_LENGTH));
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_RESPONSE_INVALID);
        } catch (FeignException e) {
            Throwable cause = e.getCause();
            log.warn(
                    "[외부 AI 서버 호출 실패] endpoint={}, status={}, causeType={}, causeMessage={}, responseSnippet={}",
                    endpoint,
                    e.status(),
                    cause == null ? "(none)" : cause.getClass().getSimpleName(),
                    sanitize(cause == null ? null : cause.getMessage(), MAX_LOG_BODY_LENGTH),
                    sanitize(e.contentUTF8(), MAX_LOG_BODY_LENGTH)
            );
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_CALL_FAILED);
        } catch (Exception e) {
            log.error("[외부 AI 응답 처리 실패] endpoint={}", endpoint, e);
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_RESPONSE_INVALID);
        }
    }

    private String sanitize(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "(omitted)";
        }
        String oneLine = text.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (oneLine.length() <= maxLength) {
            return oneLine;
        }
        return oneLine.substring(0, maxLength) + "...";
    }
}
