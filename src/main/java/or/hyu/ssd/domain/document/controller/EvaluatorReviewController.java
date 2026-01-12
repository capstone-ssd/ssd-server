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
@Tag(name = "평가자 리뷰 API")
public class EvaluatorReviewController {

    private final EvaluatorReviewService evaluatorReviewService;

    @PostMapping("/v1/documents/{documentId}/evaluator-reviews")
    @Operation(summary = "평가자 리뷰 저장/수정", description = "세 개의 고정 항목(사업타당성/사업차별성/재무적정성)에 대한 점수와 코멘트를 저장하고, 전체 평균을 계산합니다.")
    public ResponseEntity<ApiResponse<EvaluatorReviewResponse>> submit(
            @PathVariable Long documentId,
            @Valid @RequestBody EvaluatorReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewResponse dto = evaluatorReviewService.submit(documentId, user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 리뷰가 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/evaluator-reviews")
    @Operation(summary = "평가자 리뷰 조회", description = "문서에 저장된 평가자 리뷰 목록과 항목별/전체 평균 점수를 조회합니다.")
    public ResponseEntity<ApiResponse<EvaluatorReviewResponse>> get(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewResponse dto = evaluatorReviewService.get(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 리뷰가 조회되었습니다"));
    }
}
