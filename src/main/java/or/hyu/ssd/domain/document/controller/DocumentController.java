package or.hyu.ssd.domain.document.controller;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.service.DocumentService;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

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
        Long id = documentService.createDocument(user, request);
        return ResponseEntity.ok(ApiResponse.ok(new CreateDocumentResponse(id), "문서가 저장되었습니다"));
    }
}
