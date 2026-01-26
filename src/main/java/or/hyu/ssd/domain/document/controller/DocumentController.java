package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.service.DocumentService;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import or.hyu.ssd.domain.document.controller.dto.DocumentBookmarkResponse;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(
        name = "문서 API",
        description = "문서 CRUD와 즐겨찾기 토글 엔드포인트"
)
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/v1/documents")
    @Operation(
            summary = "문서 생성",
            description = """
                    ### 개요
                    - 새 문서를 저장하고 ID를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청 본문
                    - title (string, required): 공백 불가 제목
                    - content (string, required): 공백 불가 본문. 줄바꿈은 \\n 으로 이스케이프
                    - path (string, optional): 폴더 경로 (예: team/project)

                    ### 응답
                    - 200 OK
                    - data.id: 생성된 문서 ID

                    ### 오류
                    - TOKEN4030x: 토큰 누락/만료/위조
                    - REQ40001: JSON 파싱 실패
                    - SERVER50001: 내부 서버 오류
                    """
    )
    public ResponseEntity<ApiResponse<CreateDocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CreateDocumentResponse dto = documentService.createDocument(user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 저장되었습니다"));
    }


    @PutMapping("/v1/documents/{id}")
    @Operation(
            summary = "문서 수정",
            description = """
                    ### 개요
                    - 문서 ID로 부분/전체 수정합니다. Body에 포함된 필드만 변경됩니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{id}
                    - Body(JSON, optional)
                      - title (string): 새 제목
                      - content (string): 새 본문
                      - summary (string): 요약 본문
                      - details (string): 상세 요약
                      - path (string): 폴더 경로 (예: team/project)
                      - bookmark (boolean): 즐겨찾기 여부

                    ### 응답
                    - 200 OK
                    - data.id: 수정된 문서 ID

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - REQ40001: JSON 파싱 실패
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<UpdateDocumentResponse>> updateDocument(
            @Parameter(description = "수정할 문서 ID", example = "42")
            @PathVariable("id") Long id,
            @RequestBody UpdateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateDocumentResponse dto = documentService.updateDocument(id, user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 수정되었습니다"));
    }


    @DeleteMapping("/v1/documents/{id}")
    @Operation(
            summary = "문서 삭제",
            description = """
                    ### 개요
                    - 문서를 삭제하며 체크리스트/평가자 체크리스트도 함께 삭제합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{id}

                    ### 응답
                    - 200 OK
                    - data: "문서가 삭제되었습니다"

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @Parameter(description = "삭제할 문서 ID", example = "42")
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        documentService.deleteDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok("문서가 삭제되었습니다"));
    }


    @GetMapping("/v1/documents/{id}")
    @Operation(
            summary = "문서 단일 조회",
            description = """
                    ### 개요
                    - 문서 ID로 단일 문서를 조회합니다. 작성자만 접근 가능합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Path: /api/v1/documents/{id}

                    ### 응답
                    - 200 OK
                    - data:
                      - id, title, content, summary, details, path, bookmark
                      - authorId, authorName

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<GetDocumentResponse>> getDocument(
            @Parameter(description = "조회할 문서 ID", example = "42")
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GetDocumentResponse dto = documentService.getDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 조회되었습니다"));
    }


    @GetMapping("/v1/documents")
    @Operation(
            summary = "문서 목록 조회",
            description = """
                    ### 개요
                    - 로그인한 회원의 모든 문서를 정렬 옵션과 함께 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Query sort (optional, default=LATEST)
                      - LATEST: 생성일 최신순
                      - OLDEST: 생성일 오래된순
                      - NAME: 제목 오름차순
                      - MODIFIED: 수정일 최신순

                    ### 응답
                    - 200 OK
                    - data[]
                      - id: 문서 ID
                      - title: 제목
                      - path: 폴더 경로
                      - updatedAt: 마지막 수정 시각

                    ### 오류
                    - MEMBER_NOT_FOUND: 인증 정보 없음/회원 없음
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<List<DocumentListItemResponse>>> listDocuments(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "정렬 옵션", schema = @Schema(allowableValues = {"LATEST","OLDEST","NAME","MODIFIED"}))
            @RequestParam(name = "sort", defaultValue = "LATEST") DocumentSort sort
    ) {
        List<DocumentListItemResponse> list = documentService.listDocuments(user, sort);
        return ResponseEntity.ok(ApiResponse.ok(list, "문서 목록이 조회되었습니다"));
    }


    @PatchMapping("/v1/documents/{id}/bookmark")
    @Operation(
            summary = "문서 즐겨찾기 토글",
            description = """
                    ### 개요
                    - 문서의 bookmark 상태를 토글합니다. 낙관적 락 충돌 시 최대 3회까지 자동 재시도합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{id}/bookmark

                    ### 응답
                    - 200 OK
                    - data:
                      - id: 문서 ID
                      - bookmark: 토글 후 상태

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - CHECKLIST_CONFLICT: 동시 수정 충돌
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<DocumentBookmarkResponse>> toggleBookmark(
            @Parameter(description = "즐겨찾기 토글 대상 문서 ID", example = "42")
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentBookmarkResponse dto = documentService.toggleBookmark(id, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "즐겨찾기 상태가 토글되었습니다"));
    }
}
