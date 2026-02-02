package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewRequest;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewResponse;
import or.hyu.ssd.domain.document.service.EvaluatorReviewService;
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
@Tag(name = "평가자 리뷰 API", description = "평가자 점수/코멘트 저장·조회")
public class EvaluatorReviewController {

    private final EvaluatorReviewService evaluatorReviewService;

    @PostMapping("/v1/documents/{documentId}/evaluator-reviews")
    @Operation(
            summary = "평가자 리뷰 저장/수정",
            description = """
                    ### 개요
                    - 세 개의 고정 항목(사업타당성/사업차별성/재무적정성)에 대한 점수와 코멘트를 저장하거나 업데이트하고, 전체 평균을 계산합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/evaluator-reviews
                    - Body(JSON)
                      - feasibility (int 0~100): 사업타당성
                      - differentiation (int 0~100): 사업차별성
                      - financial (int 0~100): 재무적정성
                      - comment (string, optional): 코멘트

                    ### 응답
                    - 200 OK
                    - data.summary: 항목별/전체 평균 및 리뷰 수
                    - data.reviews[]: 리뷰 목록

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - REV40001: 점수 범위 오류
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorReviewResponse>> submit(
            @PathVariable Long documentId,
            @Valid @RequestBody EvaluatorReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewResponse dto = evaluatorReviewService.submit(documentId, user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 리뷰가 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/evaluator-reviews")
    @Operation(
            summary = "평가자 리뷰 조회",
            description = """
                    ### 개요
                    - 문서에 저장된 평가자 리뷰 목록과 항목별/전체 평균 점수를 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/evaluator-reviews

                    ### 응답
                    - 200 OK
                    - data.summary: 항목별/전체 평균 및 리뷰 수
                    - data.reviews[]: 리뷰 상세(리뷰어 정보, 점수, 코멘트)

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorReviewResponse>> get(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewResponse dto = evaluatorReviewService.get(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 리뷰가 조회되었습니다"));
    }
}
