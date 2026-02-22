package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.client.ExternalAiPort;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAiService {

    private final ExternalAiPort externalAiPort;
    private final DocumentRepository documentRepository;
    private final DocumentParagraphRepository documentParagraphRepository;

    public ExternalEvaluationResponse evaluate(ExternalDocumentIdRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        ExternalEvaluationRequest externalRequest = new ExternalEvaluationRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        );
        return externalAiPort.evaluate(externalRequest);
    }

    public ExternalSummarizationBasicResponse summarizeBasic(ExternalDocumentIdRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        ExternalSummarizationBasicRequest externalRequest = new ExternalSummarizationBasicRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        );
        return externalAiPort.summarizeBasic(externalRequest);
    }

    public ExternalSummarizationKeywordResponse summarizeKeyword(ExternalDocumentIdRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        ExternalSummarizationKeywordRequest externalRequest = new ExternalSummarizationKeywordRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        );
        return externalAiPort.summarizeKeyword(externalRequest);
    }

    public ExternalCheckNewTextResponse checkNewText(ExternalCheckNewTextRequest request, CustomUserDetails user) {
        Long memberId = getMemberId(user);
        int blockId = parseBlockId(request.blockId());
        boolean owned = documentParagraphRepository.existsByDocumentMemberIdAndBlockId(memberId, blockId);
        if (!owned) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return externalAiPort.checkNewText(request);
    }

    private Document getOwnedDocument(String rawDocId, CustomUserDetails user) {
        Long docId = parseDocumentId(rawDocId);
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
        Long memberId = getMemberId(user);
        if (doc.getMember() == null || doc.getMember().getId() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(memberId)) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return doc;
    }

    private Long getMemberId(CustomUserDetails user) {
        if (user == null || user.getMember() == null || user.getMember().getId() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return user.getMember().getId();
    }

    private Long parseDocumentId(String rawDocId) {
        try {
            Long docId = Long.parseLong(rawDocId);
            if (docId <= 0) {
                throw new NumberFormatException("doc_id must be positive");
            }
            return docId;
        } catch (Exception e) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND);
        }
    }

    private int parseBlockId(String rawBlockId) {
        try {
            int blockId = Integer.parseInt(rawBlockId);
            if (blockId <= 0) {
                throw new NumberFormatException("block_id must be positive");
            }
            return blockId;
        } catch (Exception e) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND);
        }
    }
}
