package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentItemResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentRequest;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentResponse;
import or.hyu.ssd.domain.document.service.DocumentCommentService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주석 API", description = "문단 주석 생성/조회")
public class DocumentCommentController {

    private final DocumentCommentService documentCommentService;

    @PostMapping("/v1/documents/{documentId}/comments")
    @Operation(
            summary = "주석 생성",
            description = """
                    ### 개요
                    - 문서의 특정 블록에 주석 코멘트를 저장합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/comments
                    - Body(JSON)
                      - blockId (int, required): 주석 대상 블록 ID
                      - comment (string, required): 주석 코멘트 본문

                    ### 응답
                    - 200 OK
                    - data.id: 생성된 주석 ID

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - MEMBER40101: 회원을 찾지 못했습니다
                    - DOC40402: 문서 블록을 찾을 수 없음
                    """
    )
    public ResponseEntity<ApiResponse<DocumentCommentResponse>> create(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentCommentResponse dto = documentCommentService.create(documentId, user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "주석이 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/comments")
    @Operation(
            summary = "주석 조회",
            description = """
                    ### 개요
                    - 문서에 저장된 주석 목록을 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/comments

                    ### 응답
                    - 200 OK
                    - data[]: 주석 목록(username, email, createdAt, content, comment)

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - MEMBER40101: 회원을 찾지 못했습니다
                    """
    )
    public ResponseEntity<ApiResponse<List<DocumentCommentItemResponse>>> list(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<DocumentCommentItemResponse> items = documentCommentService.list(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(items, "주석이 조회되었습니다"));
    }
}
