package or.hyu.ssd.api.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.api.document.response.ExternalAiBatchResponse;
import or.hyu.ssd.api.document.response.ExternalAiChecklistResponse;
import or.hyu.ssd.api.document.response.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.api.document.response.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.api.document.response.ExternalAiKeywordResponse;
import or.hyu.ssd.api.document.response.ExternalAiSummaryResponse;
import or.hyu.ssd.api.document.request.ExternalDocumentIdRequest;
import or.hyu.ssd.document.application.service.MockExternalAiService;
import or.hyu.ssd.auth.principal.CustomUserDetails;
import or.hyu.ssd.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "외부 AI Mock API", description = "클라이언트 연동용 하드코딩 응답 API")
public class MockExternalAiController {

    private final MockExternalAiService mockExternalAiService;

    @PostMapping("/v1/mock/external-ai/evaluate")
    @Operation(summary = "Mock 종합평가 반영", description = "외부 AI 평가 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiEvaluationCardResponse>> evaluate(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiEvaluationCardResponse response = ExternalAiEvaluationCardResponse.from(
                mockExternalAiService.evaluate(request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 종합평가가 조회되었습니다"));
    }

    @GetMapping("/v1/mock/external-ai/evaluate/{documentId}")
    @Operation(summary = "Mock 종합평가 조회", description = "외부 AI 평가 조회 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiEvaluationCardResponse>> getEvaluation(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiEvaluationCardResponse response = ExternalAiEvaluationCardResponse.from(
                mockExternalAiService.getEvaluation(documentId)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 종합평가가 조회되었습니다"));
    }

    @PostMapping("/v1/mock/external-ai/summarization/basic")
    @Operation(summary = "Mock 요약 반영", description = "외부 AI 요약 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiSummaryResponse>> summarizeBasic(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiSummaryResponse response = ExternalAiSummaryResponse.from(
                mockExternalAiService.summarizeBasic(request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 요약이 조회되었습니다"));
    }

    @GetMapping("/v1/mock/external-ai/summarization/basic/{documentId}")
    @Operation(summary = "Mock 요약 조회", description = "외부 AI 요약 조회 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiSummaryResponse>> getSummary(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiSummaryResponse response = ExternalAiSummaryResponse.from(
                mockExternalAiService.getSummary(documentId)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 요약이 조회되었습니다"));
    }

    @PostMapping("/v1/mock/external-ai/summarization/keyword")
    @Operation(summary = "Mock 키워드 반영", description = "외부 AI 키워드 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiKeywordResponse>> summarizeKeyword(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiKeywordResponse response = ExternalAiKeywordResponse.from(
                mockExternalAiService.summarizeKeyword(request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 키워드가 조회되었습니다"));
    }

    @GetMapping("/v1/mock/external-ai/summarization/keyword/{documentId}")
    @Operation(summary = "Mock 키워드 조회", description = "외부 AI 키워드 조회 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiKeywordResponse>> getKeyword(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiKeywordResponse response = ExternalAiKeywordResponse.from(
                mockExternalAiService.getKeyword(documentId)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 키워드가 조회되었습니다"));
    }

    @PostMapping("/v1/mock/external-ai/generate-all")
    @Operation(summary = "Mock 외부 AI 일괄 반영", description = "평가, 요약, 키워드 응답을 한 번에 하드코딩 값으로 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiBatchResponse>> generateAll(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiBatchResponse response = ExternalAiBatchResponse.from(
                mockExternalAiService.generateAll(request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 AI 결과가 일괄 조회되었습니다"));
    }

    @PostMapping("/v1/mock/external-ai/check/new-text")
    @Operation(summary = "Mock 체크리스트 반영", description = "체크리스트 반영 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiDocumentCheckResponse>> checkNewText(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiDocumentCheckResponse response = ExternalAiDocumentCheckResponse.from(
                mockExternalAiService.checkNewText(request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 체크리스트가 조회되었습니다"));
    }

    @GetMapping("/v1/mock/external-ai/check/new-text/{documentId}")
    @Operation(summary = "Mock 체크리스트 조회", description = "체크리스트 조회 응답 스펙과 동일한 하드코딩 값을 반환합니다.")
    public ResponseEntity<ApiResponse<ExternalAiChecklistResponse>> getChecklist(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiChecklistResponse response = ExternalAiChecklistResponse.from(
                mockExternalAiService.getChecklist(documentId)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Mock 외부 체크리스트가 조회되었습니다"));
    }
}
