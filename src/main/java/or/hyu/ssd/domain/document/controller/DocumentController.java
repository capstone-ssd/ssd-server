package or.hyu.ssd.domain.document.controller;

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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/v1/documents")
    public ResponseEntity<ApiResponse<CreateDocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CreateDocumentResponse dto = documentService.createDocument(user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 저장되었습니다"));
    }


    @PutMapping("/v1/documents/{id}")
    public ResponseEntity<ApiResponse<UpdateDocumentResponse>> updateDocument(
            @PathVariable("id") Long id,
            @RequestBody UpdateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateDocumentResponse dto = documentService.updateDocument(id, user, request);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 수정되었습니다"));
    }


    @DeleteMapping("/v1/documents/{id}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        documentService.deleteDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok("문서가 삭제되었습니다"));
    }


    @GetMapping("/v1/documents/{id}")
    public ResponseEntity<ApiResponse<GetDocumentResponse>> getDocument(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GetDocumentResponse dto = documentService.getDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "문서가 조회되었습니다"));
    }
}
