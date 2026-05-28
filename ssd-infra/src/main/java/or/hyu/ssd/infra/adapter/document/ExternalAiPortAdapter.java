package or.hyu.ssd.infra.adapter.document;

import feign.FeignException;
import feign.codec.DecodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.document.port.ExternalAiPort;
import or.hyu.ssd.document.port.dto.ExternalAiHealthStatus;
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
import or.hyu.ssd.common.logging.LoggingMdcKey;
import or.hyu.ssd.common.logging.MdcScope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalAiPortAdapter implements ExternalAiPort {

    private static final int MAX_LOG_BODY_LENGTH = 200;

    private final ExternalAiClient externalAiClient;

    @Override
    public ExternalAiHealthStatus health() {
        long startedAt = System.nanoTime();
        String endpoint = "GET /health";
        logExternalAi("외부 AI 서버 헬스체크를 시작합니다.", endpoint, null, 0L, "시작");
        try {
            externalAiClient.health();
            logExternalAi("외부 AI 서버 헬스체크가 완료되었습니다.", endpoint, "200", elapsedMs(startedAt), "성공");
            return ExternalAiHealthStatus.up("외부 AI 서버가 정상 응답했습니다.");
        } catch (FeignException e) {
            Throwable cause = e.getCause();
            logExternalAi("외부 AI 서버 헬스체크에 실패했습니다.", endpoint, String.valueOf(e.status()), elapsedMs(startedAt), "실패");
            log.warn(
                    "[외부 AI 서버 헬스체크 실패] endpoint=GET /health, status={}, causeType={}, causeMessage={}",
                    e.status(),
                    cause == null ? "(none)" : cause.getClass().getSimpleName(),
                    sanitize(cause == null ? null : cause.getMessage(), MAX_LOG_BODY_LENGTH)
            );
            return ExternalAiHealthStatus.down(resolveHealthFailureMessage(e.status()));
        } catch (RuntimeException e) {
            logExternalAi("외부 AI 서버 헬스체크 중 예외가 발생했습니다.", endpoint, null, elapsedMs(startedAt), "실패");
            log.warn(
                    "[외부 AI 서버 헬스체크 실패] endpoint=GET /health, causeType={}, causeMessage={}",
                    e.getClass().getSimpleName(),
                    sanitize(e.getMessage(), MAX_LOG_BODY_LENGTH)
            );
            return ExternalAiHealthStatus.down("외부 AI 서버에 연결할 수 없습니다.");
        }
    }

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
        long startedAt = System.nanoTime();
        logExternalAi("외부 AI 서버 호출을 시작합니다.", endpoint, null, 0L, "시작");
        try {
            T response = supplier.get();
            logExternalAi("외부 AI 서버 호출이 완료되었습니다.", endpoint, null, elapsedMs(startedAt), "성공");
            return response;
        } catch (DecodeException e) {
            logExternalAi("외부 AI 응답 처리에 실패했습니다.", endpoint, null, elapsedMs(startedAt), "실패");
            log.error("[외부 AI 응답 처리 실패] endpoint={}, message={}", endpoint, sanitize(e.getMessage(), MAX_LOG_BODY_LENGTH));
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_RESPONSE_INVALID);
        } catch (FeignException e) {
            Throwable cause = e.getCause();
            logExternalAi("외부 AI 서버 호출에 실패했습니다.", endpoint, String.valueOf(e.status()), elapsedMs(startedAt), "실패");
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
            logExternalAi("외부 AI 응답 처리 중 예외가 발생했습니다.", endpoint, null, elapsedMs(startedAt), "실패");
            log.error("[외부 AI 응답 처리 실패] endpoint={}", endpoint, e);
            throw new UserExceptionHandler(ErrorCode.EXTERNAL_AI_RESPONSE_INVALID);
        }
    }

    private void logExternalAi(String message, String endpoint, String status, long elapsedMs, String result) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_EXTERNAL_AI);
        values.put(LoggingMdcKey.AI_ENDPOINT, endpoint);
        values.put(LoggingMdcKey.EXTERNAL_STATUS, status);
        values.put(LoggingMdcKey.ELAPSED_MS, elapsedMs == 0L ? null : String.valueOf(elapsedMs));
        values.put(LoggingMdcKey.RESULT, result);

        try (MdcScope ignored = MdcScope.with(values)) {
            log.info(message);
        }
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String resolveHealthFailureMessage(int status) {
        if (status > 0) {
            return "외부 AI 서버가 비정상 응답을 반환했습니다. status=" + status;
        }
        return "외부 AI 서버에 연결할 수 없습니다.";
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
