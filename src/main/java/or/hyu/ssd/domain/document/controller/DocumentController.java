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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "문서 API")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/v1/documents")
    @Operation(summary = "문서 생성", description = "새 문서를 생성합니다. title, content는 필수입니다.")
    public ResponseEntity<ApiResponse<CreateDocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CreateDocumentResponse dto = documentService.createDocument(user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 저장되었습니다"));
    }


    @PutMapping("/v1/documents/{id}")
    @Operation(summary = "문서 수정", description = "문서 ID로 문서를 부분/전체 수정합니다. 제공된 필드만 반영됩니다.")
    public ResponseEntity<ApiResponse<UpdateDocumentResponse>> updateDocument(
            @PathVariable("id") Long id,
            @RequestBody UpdateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateDocumentResponse dto = documentService.updateDocument(id, user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 수정되었습니다"));
    }


    @DeleteMapping("/v1/documents/{id}")
    @Operation(summary = "문서 삭제", description = "문서 ID로 문서를 삭제합니다. 소유자만 삭제할 수 있습니다.")
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        documentService.deleteDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok("문서가 삭제되었습니다"));
    }


    @GetMapping("/v1/documents/{id}")
    @Operation(summary = "문서 단일 조회", description = "문서 ID로 단일 문서를 조회합니다. 소유자만 조회할 수 있습니다.")
    public ResponseEntity<ApiResponse<GetDocumentResponse>> getDocument(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GetDocumentResponse dto = documentService.getDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 조회되었습니다"));
    }


    @GetMapping("/v1/documents")
    @Operation(summary = "문서 목록 조회", description = "회원의 모든 문서를 정렬 옵션과 함께 조회합니다. <br>" +
            "정렬 옵션: LATEST(최신순), OLDEST(오래된순), NAME(제목순), MODIFIED(최근수정순)")
    public ResponseEntity<ApiResponse<List<DocumentListItemResponse>>> listDocuments(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "정렬 옵션", schema = @Schema(allowableValues = {"LATEST","OLDEST","NAME","MODIFIED"}))
            @RequestParam(name = "sort", defaultValue = "LATEST") DocumentSort sort
    ) {
        List<DocumentListItemResponse> list = documentService.listDocuments(user, sort);
        return ResponseEntity.ok(ApiResponse.ok(list, "문서 목록이 조회되었습니다"));
    }
}
