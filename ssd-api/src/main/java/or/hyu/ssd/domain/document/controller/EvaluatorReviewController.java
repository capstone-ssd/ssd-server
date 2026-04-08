package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewCreateRequest;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewDetailResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewIdResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewListResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorReviewUpdateRequest;
import or.hyu.ssd.domain.document.service.EvaluatorReviewService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "리뷰 API", description = "문서 평가 리뷰 생성/수정/조회/삭제")
public class EvaluatorReviewController {

    private final EvaluatorReviewService evaluatorReviewService;

    @PostMapping("/v1/documents/{documentId}/reviews")
    @Operation(
            summary = "리뷰 생성",
            description = """
                    ### 개요
                    - 로그인한 사용자가 문서에 대해 자신의 리뷰 1건을 생성합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/reviews
                    - Body(JSON)
                      - feasibility: 사업타당성 점수 (0~100)
                      - differentiation: 사업차별성 점수 (0~100)
                      - financial: 재무적정성 점수 (0~100)
                      - comment: 상세의견

                    ### 제약
                    - 한 사용자는 문서당 리뷰 1건만 작성할 수 있습니다.
                    - 이미 리뷰가 있으면 409 Conflict를 반환합니다.
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorReviewIdResponse>> create(
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @Valid @RequestBody EvaluatorReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewIdResponse response = EvaluatorReviewIdResponse.from(
                evaluatorReviewService.create(documentId, user, request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "리뷰가 저장되었습니다"));
    }

    @PutMapping("/v1/documents/{documentId}/reviews")
    @Operation(
            summary = "내 리뷰 수정",
            description = """
                    ### 개요
                    - 로그인한 사용자가 자신이 작성한 리뷰를 수정합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorReviewDetailResponse>> update(
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @Valid @RequestBody EvaluatorReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewDetailResponse response = EvaluatorReviewDetailResponse.from(
                evaluatorReviewService.update(documentId, user, request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "리뷰가 수정되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/reviews/me")
    @Operation(
            summary = "내 리뷰 상세 조회",
            description = """
                    ### 개요
                    - 로그인한 사용자가 자신이 작성한 리뷰의 상세 정보를 조회합니다.
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorReviewDetailResponse>> getMyReview(
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewDetailResponse response = EvaluatorReviewDetailResponse.from(
                evaluatorReviewService.getMyReview(documentId, user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "리뷰가 조회되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/reviews")
    @Operation(
            summary = "문서 리뷰 목록 조회",
            description = """
                    ### 개요
                    - 문서 작성자가 해당 문서의 전체 리뷰 목록을 조회합니다.
                    - 모든 리뷰의 종합평점 평균과 함께, 각 리뷰의 상세 정보(작성자, 이메일, 수정시각, 항목별 점수, 코멘트)를 반환합니다.
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorReviewListResponse>> list(
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorReviewListResponse response = EvaluatorReviewListResponse.from(
                evaluatorReviewService.list(documentId, user)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "리뷰 목록이 조회되었습니다"));
    }

    @DeleteMapping("/v1/documents/{documentId}/reviews")
    @Operation(
            summary = "내 리뷰 삭제",
            description = """
                    ### 개요
                    - 로그인한 사용자가 자신이 작성한 리뷰를 삭제합니다.
                    - 삭제 후 문서 평균 점수는 즉시 재집계됩니다.
                    """
    )
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        evaluatorReviewService.delete(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok("리뷰가 삭제되었습니다"));
    }
}
