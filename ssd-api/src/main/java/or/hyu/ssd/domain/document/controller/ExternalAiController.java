package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.domain.document.service.ExternalAiService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/v1/external-ai/evaluate")
    @Operation(
            summary = "외부 사업계획서 종합평가 호출",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/evaluate` API를 호출해 종합평가 결과를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/evaluate
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 처리
                    - 요청받은 `docId`로 문서 소유권을 검증한 뒤,
                      서버가 DB에서 본문을 조회하여 외부 API에 전달합니다.

                    ### 응답
                    - 200 OK
                    - data: 외부 서버 EvaluationResponse 원문

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalEvaluationResponse>> evaluate(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalEvaluationResponse response = externalAiService.evaluate(request, user);
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 종합평가를 성공적으로 호출했습니다"));
    }

    @PostMapping("/v1/external-ai/summarization/basic")
    @Operation(
            summary = "외부 사업계획서 요약 호출",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/summarization/Basic` API를 호출해 요약 결과를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/summarization/basic
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 처리
                    - 요청받은 `docId`로 문서 소유권을 검증한 뒤,
                      서버가 DB에서 본문을 조회하여 외부 API에 전달합니다.

                    ### 응답
                    - 200 OK
                    - data: 외부 서버 SummarizationResponse 원문

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalSummarizationBasicResponse>> summarizeBasic(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalSummarizationBasicResponse response = externalAiService.summarizeBasic(request, user);
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 요약 API를 성공적으로 호출했습니다"));
    }

    @PostMapping("/v1/external-ai/summarization/keyword")
    @Operation(
            summary = "외부 사업계획서 키워드 추출 호출",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/summarization/Keyword` API를 호출해 키워드를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/summarization/keyword
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 처리
                    - 요청받은 `docId`로 문서 소유권을 검증한 뒤,
                      서버가 DB에서 본문을 조회하여 외부 API에 전달합니다.

                    ### 응답
                    - 200 OK
                    - data: 외부 서버 SummarizationKeywordResponse 원문

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalSummarizationKeywordResponse>> summarizeKeyword(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalSummarizationKeywordResponse response = externalAiService.summarizeKeyword(request, user);
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 키워드 API를 성공적으로 호출했습니다"));
    }

    @PostMapping("/v1/external-ai/check/new-text")
    @Operation(
            summary = "외부 체크리스트 평가 호출",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/check/new-text` API를 호출해 블록 단위 체크리스트 평가를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/check/new-text
                    - Body(JSON)
                      - block_id (string): 블록 식별자
                      - block (string): 평가할 블록 본문

                    ### 응답
                    - 200 OK
                    - data: 외부 서버 CheckNewTextResponse 원문

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalCheckNewTextResponse>> checkNewText(
            @Valid @RequestBody ExternalCheckNewTextRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalCheckNewTextResponse response = externalAiService.checkNewText(request, user);
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 체크리스트 평가 API를 성공적으로 호출했습니다"));
    }
}
