package or.hyu.ssd.infra.adapter.document;

import feign.FeignException;
import feign.codec.DecodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.document.port.ExternalAiPort;
import or.hyu.ssd.document.port.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.document.port.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.document.port.dto.ExternalEvaluationRequest;
import or.hyu.ssd.document.port.dto.ExternalEvaluationResponse;
import or.hyu.ssd.document.port.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.document.port.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.document.port.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.document.port.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import or.hyu.ssd.external.ai.client.ExternalAiClient;
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
        return callExternalApi(
                () -> toDomain(externalAiClient.evaluate(toExternal(request))),
                "POST /evaluate"
        );
    }

    @Override
    public ExternalSummarizationBasicResponse summarizeBasic(ExternalSummarizationBasicRequest request) {
        return callExternalApi(
                () -> toDomain(externalAiClient.summarizeBasic(toExternal(request))),
                "POST /summarization/Basic"
        );
    }

    @Override
    public ExternalSummarizationKeywordResponse summarizeKeyword(ExternalSummarizationKeywordRequest request) {
        return callExternalApi(
                () -> toDomain(externalAiClient.summarizeKeyword(toExternal(request))),
                "POST /summarization/Keyword"
        );
    }

    @Override
    public ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request) {
        return callExternalApi(
                () -> toDomain(externalAiClient.checkNewText(toExternal(request))),
                "POST /check/new-text"
        );
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

    private or.hyu.ssd.external.ai.dto.ExternalEvaluationRequest toExternal(ExternalEvaluationRequest request) {
        return new or.hyu.ssd.external.ai.dto.ExternalEvaluationRequest(request.docId(), request.doc());
    }

    private or.hyu.ssd.external.ai.dto.ExternalSummarizationBasicRequest toExternal(ExternalSummarizationBasicRequest request) {
        return new or.hyu.ssd.external.ai.dto.ExternalSummarizationBasicRequest(request.docId(), request.doc());
    }

    private or.hyu.ssd.external.ai.dto.ExternalSummarizationKeywordRequest toExternal(ExternalSummarizationKeywordRequest request) {
        return new or.hyu.ssd.external.ai.dto.ExternalSummarizationKeywordRequest(request.docId(), request.doc());
    }

    private or.hyu.ssd.external.ai.dto.ExternalCheckNewTextRequest toExternal(ExternalCheckNewTextRequest request) {
        return new or.hyu.ssd.external.ai.dto.ExternalCheckNewTextRequest(
                request.docId(),
                request.blocks().stream()
                        .map(block -> new or.hyu.ssd.external.ai.dto.ExternalCheckNewTextBlockRequest(block.blockId(), block.block()))
                        .toList()
        );
    }

    private ExternalEvaluationResponse toDomain(or.hyu.ssd.external.ai.dto.ExternalEvaluationResponse response) {
        return new ExternalEvaluationResponse(
                response.docId(),
                toDomain(response.evaluationReport()),
                response.checkList()
        );
    }

    private ExternalSummarizationBasicResponse toDomain(or.hyu.ssd.external.ai.dto.ExternalSummarizationBasicResponse response) {
        return new ExternalSummarizationBasicResponse(response.docId(), response.summary(), response.small());
    }

    private ExternalSummarizationKeywordResponse toDomain(or.hyu.ssd.external.ai.dto.ExternalSummarizationKeywordResponse response) {
        return new ExternalSummarizationKeywordResponse(response.docId(), response.keyword());
    }

    private ExternalCheckNewTextResponse toDomain(or.hyu.ssd.external.ai.dto.ExternalCheckNewTextResponse response) {
        return new ExternalCheckNewTextResponse(response.blockId(), response.checkList());
    }

    private or.hyu.ssd.document.port.dto.ExternalEvaluationReportResponse toDomain(
            or.hyu.ssd.external.ai.dto.ExternalEvaluationReportResponse response
    ) {
        if (response == null) {
            return null;
        }
        return new or.hyu.ssd.document.port.dto.ExternalEvaluationReportResponse(
                toDomain(response.teamEvaluator()),
                toDomain(response.solEvaluator()),
                toDomain(response.problemEvaluator()),
                toDomain(response.businessModelEvaluator()),
                toDomain(response.scaleUpEvaluator())
        );
    }

    private or.hyu.ssd.document.port.dto.ExternalEvaluatorMetricResponse toDomain(
            or.hyu.ssd.external.ai.dto.ExternalEvaluatorMetricResponse response
    ) {
        if (response == null) {
            return null;
        }
        return new or.hyu.ssd.document.port.dto.ExternalEvaluatorMetricResponse(
                response.averageScore(),
                response.finalReview()
        );
    }
}
