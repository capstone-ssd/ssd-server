package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentEvaluationResponse;
import or.hyu.ssd.domain.document.service.EvaluationService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "상세평가 API")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/v1/documents/{documentId}/evaluation/generate")
    @Operation(summary = "상세평가 생성", description = "문서의 내용을 기반으로 상세 평가를 생성/저장합니다. 기존 평가가 있으면 삭제 후 덮어씁니다.")
    public ResponseEntity<ApiResponse<DocumentEvaluationResponse>> generate(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentEvaluationResponse dto = evaluationService.generate(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "상세 평가가 생성되어 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/evaluation")
    @Operation(summary = "상세평가 조회", description = "문서에 저장된 상세 평가를 조회합니다.")
    public ResponseEntity<ApiResponse<DocumentEvaluationResponse>> get(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentEvaluationResponse dto = evaluationService.get(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "상세 평가가 조회되었습니다"));
    }
}
