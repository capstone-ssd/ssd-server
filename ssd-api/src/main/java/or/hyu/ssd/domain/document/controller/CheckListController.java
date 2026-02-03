package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CheckListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.GenerateChecklistResponse;
import or.hyu.ssd.domain.document.service.CheckListService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "체크리스트 API", description = "사용자 체크리스트 생성/조회/토글/삭제")
public class CheckListController {

    private final CheckListService checkListService;

    @GetMapping("/v1/documents/{documentId}/checklists")
    @Operation(
            summary = "체크리스트 조회",
            description = """
                    ### 개요
                    - 문서에 속한 체크리스트 목록을 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/checklists

                    ### 응답
                    - 200 OK
                    - data.items[]: 체크리스트 항목(id, content, checked)

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<List<CheckListItemResponse>>> list(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<CheckListItemResponse> items = checkListService.listByDocument(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(items, "체크리스트가 조회되었습니다"));
    }

    @PatchMapping("/v1/checklists/{id}")
    @Operation(
            summary = "체크리스트 토글",
            description = """
                    ### 개요
                    - 체크리스트의 체크 상태를 토글합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/checklists/{id}

                    ### 응답
                    - 200 OK
                    - data: 토글된 항목(id, content, checked)

                    ### 오류
                    - CHECKLIST_NOT_FOUND: 체크리스트 없음
                    - CHECKLIST_FORBIDDEN: 권한 없음
                    - CHECKLIST_CONFLICT: 동시 수정 충돌
                    """
    )
    public ResponseEntity<ApiResponse<CheckListItemResponse>> toggleChecked(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CheckListItemResponse dto = checkListService.toggleChecked(id, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "체크 상태가 토글되었습니다"));
    }

    @DeleteMapping("/v1/checklists/{id}")
    @Operation(
            summary = "체크리스트 삭제",
            description = """
                    ### 개요
                    - 단일 체크리스트 항목을 삭제합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/checklists/{id}

                    ### 응답
                    - 200 OK
                    - data: "체크리스트가 삭제되었습니다"

                    ### 오류
                    - CHECKLIST_NOT_FOUND: 체크리스트 없음
                    - CHECKLIST_FORBIDDEN: 권한 없음
                    """
    )
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        checkListService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.ok("체크리스트가 삭제되었습니다"));
    }

    @PostMapping("/v1/documents/{documentId}/checklists/generate")
    @Operation(
            summary = "체크리스트 생성 및 저장",
            description = """
                    ### 개요
                    - 문서 content를 기반으로 체크리스트를 생성하고 저장합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/checklists/generate

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.items[]: 생성된 체크리스트 항목들

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<GenerateChecklistResponse>> generate(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GenerateChecklistResponse dto = checkListService.generate(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "체크리스트가 생성되어 저장되었습니다"));
    }
}
