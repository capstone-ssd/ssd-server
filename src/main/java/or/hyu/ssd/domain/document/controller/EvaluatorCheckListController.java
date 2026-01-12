package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorCheckListResponse;
import or.hyu.ssd.domain.document.service.EvaluatorCheckListService;
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
@Tag(name = "평가자 체크리스트 API")
public class EvaluatorCheckListController {

    private final EvaluatorCheckListService evaluatorCheckListService;

    @PostMapping("/v1/documents/{documentId}/evaluator-checklists/generate")
    @Operation(summary = "평가자 체크리스트 생성", description = "문서 내용을 기반으로 평가자 체크리스트를 생성하고, AI 판단에 따른 충족 여부를 함께 저장합니다.")
    public ResponseEntity<ApiResponse<EvaluatorCheckListResponse>> generate(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorCheckListResponse dto = evaluatorCheckListService.generate(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 체크리스트가 생성되어 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/evaluator-checklists")
    @Operation(summary = "평가자 체크리스트 조회", description = "저장된 평가자 체크리스트와 각 항목의 충족 여부를 조회합니다.")
    public ResponseEntity<ApiResponse<EvaluatorCheckListResponse>> list(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorCheckListResponse dto = evaluatorCheckListService.list(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 체크리스트가 조회되었습니다"));
    }
}
