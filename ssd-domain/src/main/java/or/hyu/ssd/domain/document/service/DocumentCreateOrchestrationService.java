package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCreateOrchestrationService {

    private final DocumentService documentService;
    private final ExternalAiService externalAiService;

    public CreateDocumentResponse createDocumentWithAi(CustomUserDetails user, CreateDocumentRequest request) {
        CreateDocumentResponse response = documentService.createDocument(user, request);
        ExternalDocumentIdRequest documentIdRequest = new ExternalDocumentIdRequest(String.valueOf(response.id()));

        try {
            externalAiService.evaluate(documentIdRequest, user);
            externalAiService.summarizeBasic(documentIdRequest, user);
            externalAiService.summarizeKeyword(documentIdRequest, user);
            return response;
        } catch (RuntimeException e) {
            compensateCreatedDocument(response.id(), user, e);
            throw e;
        }
    }

    private void compensateCreatedDocument(Long documentId, CustomUserDetails user, RuntimeException originalException) {
        try {
            documentService.deleteDocument(documentId, user);
        } catch (RuntimeException compensationException) {
            log.error("문서 생성 보상 삭제에 실패했습니다. documentId={}", documentId, compensationException);
            originalException.addSuppressed(compensationException);
        }
    }
}
