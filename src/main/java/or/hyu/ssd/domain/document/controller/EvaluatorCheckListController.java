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
@Tag(name = "평가자 체크리스트 API", description = "AI 평가자 체크리스트 생성/조회")
public class EvaluatorCheckListController {

    private final EvaluatorCheckListService evaluatorCheckListService;

    @PostMapping("/v1/documents/{documentId}/evaluator-checklists/generate")
    @Operation(
            summary = "평가자 체크리스트 생성",
            description = """
                    ### 개요
                    - 문서 내용을 기반으로 평가자 체크리스트를 생성하고, AI 판단에 따른 충족 여부를 저장합니다. 기존 데이터는 덮어씁니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/evaluator-checklists/generate

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.items[]: 평가 항목 및 충족 여부

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - EVALUATOR_CHECKLIST_PARSE_ERROR: 생성 응답 파싱 실패
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorCheckListResponse>> generate(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorCheckListResponse dto = evaluatorCheckListService.generate(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 체크리스트가 생성되어 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/evaluator-checklists")
    @Operation(
            summary = "평가자 체크리스트 조회",
            description = """
                    ### 개요
                    - 저장된 평가자 체크리스트와 각 항목의 충족 여부를 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/evaluator-checklists

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.items[]: 평가 항목 및 충족 여부

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<EvaluatorCheckListResponse>> list(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        EvaluatorCheckListResponse dto = evaluatorCheckListService.list(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "평가자 체크리스트가 조회되었습니다"));
    }
}
