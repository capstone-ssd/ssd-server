package or.hyu.ssd.api.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.api.document.response.ExternalAiChecklistResponse;
import or.hyu.ssd.api.document.response.ExternalAiBatchResponse;
import or.hyu.ssd.api.document.response.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.api.document.response.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.api.document.response.ExternalAiKeywordResponse;
import or.hyu.ssd.api.document.response.ExternalAiSummaryResponse;
import or.hyu.ssd.api.document.request.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.document.service.ExternalAiBatchService;
import or.hyu.ssd.domain.document.service.ExternalAiService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
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
@Tag(name = "외부 AI 연동 API", description = "외부 AI 평가/요약 서버 연동")
public class ExternalAiController {

    private final ExternalAiService externalAiService;
    private final ExternalAiBatchService externalAiBatchService;

    @PostMapping("/v1/external-ai/evaluate")
    @Operation(
            summary = "외부 사업계획서 종합평가 반영",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/evaluate` API를 호출하고 결과를 DB에 반영합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/evaluate
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 처리
                    - 요청받은 `docId`로 문서 소유권을 검증합니다.
                    - 외부 AI 서버를 호출해 종합평가 결과를 받은 뒤, DB에 저장합니다.

                    ### 응답
                    - 200 OK
                    - data: DB에 반영된 최신 종합평가 결과
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiEvaluationCardResponse>> evaluate(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiEvaluationCardResponse response = ExternalAiEvaluationCardResponse.from(
                externalAiService.evaluate(request.toCommand(), user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 종합평가를 성공적으로 반영했습니다"));
    }

    @GetMapping("/v1/external-ai/evaluate/{documentId}")
    @Operation(
            summary = "저장된 외부 사업계획서 종합평가 조회",
            description = """
                    ### 개요
                    - 현재 DB에 저장된 종합평가 결과만 조회합니다.
                    - 외부 AI 서버는 호출하지 않습니다.
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiEvaluationCardResponse>> getEvaluation(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiEvaluationCardResponse response = ExternalAiEvaluationCardResponse.from(
                externalAiService.getEvaluation(documentId, user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "저장된 외부 종합평가가 조회되었습니다"));
    }

    @PostMapping("/v1/external-ai/summarization/basic")
    @Operation(
            summary = "외부 사업계획서 요약 반영",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/summarization/Basic` API를 호출하고 요약 결과를 DB에 반영합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/summarization/basic
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 응답
                    - 200 OK
                    - data.summary: 저장된 요약
                    - data.shortSummary: 저장된 짧은 요약
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiSummaryResponse>> summarizeBasic(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiSummaryResponse response = ExternalAiSummaryResponse.from(
                externalAiService.summarizeBasic(request.toCommand(), user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 요약 API를 성공적으로 반영했습니다"));
    }

    @GetMapping("/v1/external-ai/summarization/basic/{documentId}")
    @Operation(
            summary = "저장된 외부 사업계획서 요약 조회",
            description = """
                    ### 개요
                    - 현재 DB에 저장된 요약과 짧은 요약만 조회합니다.
                    - 외부 AI 서버는 호출하지 않습니다.
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiSummaryResponse>> getSummary(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiSummaryResponse response = ExternalAiSummaryResponse.from(
                externalAiService.getSummary(documentId, user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "저장된 외부 요약이 조회되었습니다"));
    }

    @PostMapping("/v1/external-ai/summarization/keyword")
    @Operation(
            summary = "외부 사업계획서 키워드 추출 반영",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/summarization/Keyword` API를 호출하고 키워드를 DB에 반영합니다.
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiKeywordResponse>> summarizeKeyword(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiKeywordResponse response = ExternalAiKeywordResponse.from(
                externalAiService.summarizeKeyword(request.toCommand(), user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 키워드 API를 성공적으로 반영했습니다"));
    }

    @GetMapping("/v1/external-ai/summarization/keyword/{documentId}")
    @Operation(
            summary = "저장된 외부 사업계획서 키워드 조회",
            description = """
                    ### 개요
                    - 현재 DB에 저장된 키워드만 조회합니다.
                    - 외부 AI 서버는 호출하지 않습니다.
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiKeywordResponse>> getKeyword(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiKeywordResponse response = ExternalAiKeywordResponse.from(
                externalAiService.getKeyword(documentId, user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "저장된 외부 키워드가 조회되었습니다"));
    }


    @PostMapping("/v1/external-ai/generate-all")
    @Operation(
            summary = "외부 AI 일괄 반영",
            description = """
                    ### 개요
                    - 외부 AI `evaluate`, `summarization/basic`, `summarization/keyword`를 순차 호출해 문서에 한 번에 반영합니다.
                    - 문서 생성 API와 분리된 후처리용 API입니다.
                    - `/check/new-text` 체크리스트 API는 호출하지 않습니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/generate-all
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.evaluation: 저장된 종합평가 결과
                    - data.summary: 저장된 요약 결과
                    - data.keyword: 저장된 키워드 결과
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiBatchResponse>> generateAll(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiBatchResponse response = ExternalAiBatchResponse.from(
                externalAiBatchService.generateAll(request.toCommand(), user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 AI 결과가 일괄 반영되었습니다"));
    }

    @PostMapping("/v1/external-ai/check/new-text")
    @Operation(
            summary = "외부 체크리스트 평가 반영",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/check/new-text` API를 호출하고 변경된 블록 기준 체크리스트 결과를 DB에 반영합니다.

                    ### 처리
                    - 이전 스냅샷에 없는 blockId다 -> 변경
                    - 같은 blockId인데 content가 다르다 -> 변경
                    - 같은 blockId이고 content가 같다 -> 변경 아님
                    - 변경된 block만 외부 AI 서버에 전달합니다.
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiDocumentCheckResponse>> checkNewText(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiDocumentCheckResponse response = ExternalAiDocumentCheckResponse.from(
                externalAiService.checkNewText(request.toCommand(), user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 체크리스트 평가 API를 성공적으로 반영했습니다"));
    }

    @GetMapping("/v1/external-ai/check/new-text/{documentId}")
    @Operation(
            summary = "저장된 외부 체크리스트 조회",
            description = """
                    ### 개요
                    - 현재 DB에 저장된 체크리스트 값만 조회합니다.
                    - 외부 AI 서버는 호출하지 않습니다.
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiChecklistResponse>> getChecklist(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiChecklistResponse response = ExternalAiChecklistResponse.from(
                externalAiService.getChecklist(documentId, user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "저장된 외부 체크리스트가 조회되었습니다"));
    }
}
