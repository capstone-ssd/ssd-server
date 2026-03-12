package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
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
                    - 외부 평가 결과의 5개 축을 SSD 도메인 응답으로 가공하여 반환합니다.

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.totalScore: 5개 평가 축 평균 점수
                    - data.problemRecognition / feasibility / growthStrategy / businessModel / teamComposition

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiEvaluationCardResponse>> evaluate(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiEvaluationCardResponse response = externalAiService.evaluate(request, user);
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
                    - 생성된 요약을 문서에 저장한 뒤 SSD 기준 응답으로 반환합니다.

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.summary: 저장된 요약
                    - data.shortSummary: 외부 서버 short summary

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiSummaryResponse>> summarizeBasic(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiSummaryResponse response = externalAiService.summarizeBasic(request, user);
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
                    - 추출한 키워드를 SSD 도메인 응답으로 가공하여 반환합니다.

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.keyword: 추출된 키워드

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiKeywordResponse>> summarizeKeyword(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiKeywordResponse response = externalAiService.summarizeKeyword(request, user);
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 키워드 API를 성공적으로 호출했습니다"));
    }

    @PostMapping("/v1/external-ai/check/new-text")
    @Operation(
            summary = "외부 체크리스트 평가 호출",
            description = """
                    ### 개요
                    - 외부 AI 서버의 `/check/new-text` API를 호출해 문서의 변경 블록 기준 체크리스트 평가를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/external-ai/check/new-text
                    - Body(JSON)
                      - docId (string): 문서 식별자

                    ### 처리
                    - 요청받은 `docId`로 문서 소유권을 검증합니다.
                    - 서버가 마지막 AI 체크 스냅샷과 현재 문단 목록을 비교해 변경된 블록만 추출합니다.
                    - 변경된 블록 목록만 외부 AI 서버에 전달한 뒤, 응답 체크리스트를 누적 저장합니다.

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.changedBlockIds: 외부 AI 서버로 전달된 변경 블록 ID 목록
                    - data.checkList: 문서에 누적 저장된 체크리스트 상태

                    ### 오류
                    - AI50201: 외부 AI 서버 호출 실패
                    - AI50202: 외부 AI 서버 응답 처리 실패
                    """
    )
    public ResponseEntity<ApiResponse<ExternalAiDocumentCheckResponse>> checkNewText(
            @Valid @RequestBody ExternalDocumentIdRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ExternalAiDocumentCheckResponse response = externalAiService.checkNewText(request, user);
        return ResponseEntity.ok(ApiResponse.ok(response, "외부 체크리스트 평가 API를 성공적으로 호출했습니다"));
    }
}
