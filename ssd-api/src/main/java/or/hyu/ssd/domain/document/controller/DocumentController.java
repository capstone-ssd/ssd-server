package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentBookmarkResponse;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentImageMetaRequest;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.service.DocumentCommandService;
import or.hyu.ssd.domain.document.service.DocumentQueryService;
import or.hyu.ssd.domain.document.service.support.DocumentImageUploadPart;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import or.hyu.ssd.domain.document.usecase.result.DocumentDetailResult;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DocumentException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "문서 API",
        description = "문서 CRUD와 즐겨찾기 토글 엔드포인트"
)
public class DocumentController {

    private final DocumentCommandService documentCommandService;
    private final DocumentQueryService documentQueryService;

    @PostMapping(value = "/v1/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "문서 생성",
            description = """
                    ### 개요
                    - 새 문서를 저장하고 ID를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청 본문
                    - title (string, optional): 제목. 없으면 text/paragraphs 첫 항목으로 자동 생성
                    - text (string, required): 공백 불가 본문. 줄바꿈은 \\n 으로 이스케이프
                    - paragraphs (array, optional): 문단 메타데이터 배열 (content, role, blockId만 입력. pageNumber는 생성 API에서 받지 않으며 1로 저장)
                      - role 허용값: "", "#", "##", "###", "####", "#####", "######"
                    - folderId (number, optional): 폴더 ID (없으면 루트)

                    ### 응답
                    - 200 OK
                    - data.id: 생성된 문서 ID

                    ### 후처리
                    - 외부 AI 종합평가/요약/키워드 생성은 별도 `POST /api/v1/external-ai/generate-all` API를 호출해야 합니다.
                    - 생성 API는 문서 저장만 담당합니다.

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
        CreateDocumentResponse dto = CreateDocumentResponse.from(documentCommandService.createDocument(user, request.toCommand()));
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 저장되었습니다"));
    }

    @PostMapping(value = "/v1/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "문서 생성 (이미지 포함)",
            description = """
                    ### 개요
                    - `request` 파트의 문서 JSON과 이미지 파일을 함께 받아 문서를 저장합니다.

                    ### multipart 파트
                    - request (application/json, required): 문서 생성 요청 JSON
                    - imageMetas (application/json, optional): blobKey/blockId 매핑 배열
                    - files (file[], optional): 실제 이미지 파일 배열
                    """
    )
    public ResponseEntity<ApiResponse<CreateDocumentResponse>> createDocumentWithImages(
            @Valid @RequestPart("request") CreateDocumentRequest request,
            @Valid @RequestPart(value = "imageMetas", required = false) List<DocumentImageMetaRequest> imageMetas,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CreateDocumentResponse dto = CreateDocumentResponse.from(
                documentCommandService.createDocument(user, request.toCommand(), toImageUploadParts(imageMetas, files))
        );
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 저장되었습니다"));
    }


    @PutMapping(value = "/v1/documents/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "문서 수정",
            description = """
                    ### 개요
                    - 문서 ID로 본문/제목/문단/폴더를 생성 API와 동일한 스펙으로 교체합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{id}
                    - Body(JSON, required)
                      - title (string, optional): 제목. null이면 기존 제목 유지
                      - text (string, required): 새 본문
                      - paragraphs (array, optional): 문단 메타데이터 배열 (content, role, blockId만 입력. pageNumber는 수정 API에서도 받지 않으며 1로 저장)
                        - role 허용값: "", "#", "##", "###", "####", "#####", "######"
                        - 기존 문단은 기존 blockId를 그대로 보내야 하며, 새 문단은 새 blockId를 사용합니다.
                        - 요청에서 빠진 blockId에 달린 주석은 함께 삭제됩니다.

                    ### 제외 필드
                    - folderId, summary, details, shortSummary, keywords, 외부 AI 평가값, checklist, bookmark는 이 API로 수정하지 않습니다.
                    - 문서 이동은 별도 폴더 API로 처리합니다.

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
            @PathVariable("id") @Positive(message = "문서 ID는 1 이상이어야 합니다") Long id,
            @Valid @RequestBody UpdateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateDocumentResponse dto = UpdateDocumentResponse.from(documentCommandService.updateDocument(id, user, request.toCommand()));
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 수정되었습니다"));
    }

    @PutMapping(value = "/v1/documents/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "문서 수정 (이미지 포함)",
            description = """
                    ### 개요
                    - `request` 파트의 문서 JSON과 이미지 파일을 함께 받아 문서를 수정합니다.
                    - 기존 이미지 블록은 `url`로 유지하고, 새 이미지 블록은 `blobKey + files`로 업로드합니다.
                    """
    )
    public ResponseEntity<ApiResponse<UpdateDocumentResponse>> updateDocumentWithImages(
            @Parameter(description = "수정할 문서 ID", example = "42")
            @PathVariable("id") @Positive(message = "문서 ID는 1 이상이어야 합니다") Long id,
            @Valid @RequestPart("request") UpdateDocumentRequest request,
            @Valid @RequestPart(value = "imageMetas", required = false) List<DocumentImageMetaRequest> imageMetas,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateDocumentResponse dto = UpdateDocumentResponse.from(
                documentCommandService.updateDocument(id, user, request.toCommand(), toImageUploadParts(imageMetas, files))
        );
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
            @PathVariable("id") @Positive(message = "문서 ID는 1 이상이어야 합니다") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        documentCommandService.deleteDocument(id, user);
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
                      - id, title, text, paragraphs, summary, details, folderId, bookmark
                      - authorId, authorName

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<GetDocumentResponse>> getDocument(
            @Parameter(description = "조회할 문서 ID", example = "42")
            @PathVariable("id") @Positive(message = "문서 ID는 1 이상이어야 합니다") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentDetailResult result = documentQueryService.getDocument(id, user);
        GetDocumentResponse dto = GetDocumentResponse.from(result);
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
                    - Query folderId (optional)
                      - 없으면 전체
                      - 0이면 루트

                    ### 응답
                    - 200 OK
                    - data[]
                      - id: 문서 ID
                      - title: 제목
                      - folderId: 폴더 ID (없으면 루트)
                      - updatedAt: 마지막 수정 시각

                    ### 오류
                    - MEMBER_NOT_FOUND: 인증 정보 없음/회원 없음
                    - TOKEN4030x: 토큰 누락/만료/위조
                    """
    )
    public ResponseEntity<ApiResponse<List<DocumentListItemResponse>>> listDocuments(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "정렬 옵션", schema = @Schema(allowableValues = {"LATEST","OLDEST","NAME","MODIFIED"}))
            @RequestParam(name = "sort", defaultValue = "LATEST") DocumentSort sort,
            @Parameter(description = "폴더 ID (없으면 전체, 0이면 루트)")
            @RequestParam(name = "folderId", required = false) @PositiveOrZero(message = "folderId는 0 이상이어야 합니다") Long folderId
    ) {
        List<DocumentListItemResponse> list = documentQueryService.listDocuments(user, sort, folderId).stream()
                .map(DocumentListItemResponse::from)
                .toList();
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
            @PathVariable("id") @Positive(message = "문서 ID는 1 이상이어야 합니다") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentBookmarkResponse dto = DocumentBookmarkResponse.from(documentCommandService.toggleBookmark(id, user));
        return ResponseEntity.ok(ApiResponse.ok(dto, "즐겨찾기 상태가 토글되었습니다"));
    }

    private List<DocumentImageUploadPart> toImageUploadParts(
            List<DocumentImageMetaRequest> imageMetas,
            List<MultipartFile> files
    ) {
        boolean metasEmpty = imageMetas == null || imageMetas.isEmpty();
        boolean filesEmpty = files == null || files.isEmpty();
        if (metasEmpty && filesEmpty) {
            return List.of();
        }
        if (metasEmpty || filesEmpty || imageMetas.size() != files.size()) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas와 files는 같은 개수로 전달되어야 합니다");
        }

        List<DocumentImageUploadPart> uploadParts = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            DocumentImageMetaRequest meta = imageMetas.get(i);
            try {
                uploadParts.add(new DocumentImageUploadPart(
                        meta.blobKey(),
                        meta.blockId(),
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                ));
            } catch (Exception exception) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "이미지 파일을 읽는 데 실패했습니다");
            }
        }
        return uploadParts;
    }
}
