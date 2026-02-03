package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.ai.util.AiTextClient;
import or.hyu.ssd.domain.ai.util.PromptComposer;
import or.hyu.ssd.domain.document.controller.dto.DocumentDetailsResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.PromptProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DetailsService {

    private final DocumentRepository documentRepository;
    private final PromptProperties promptProperties;
    private final AiTextClient aiTextClient;

    public DocumentDetailsResponse generate(Long documentId, CustomUserDetails user) {
        Document document = getOwnedDocument(documentId, user);

        // 기존 상세 요약 초기화 후 재생성
        document.updateDetails(null);

        String content = document.getContent();
        if (content == null || content.isBlank()) {
            return DocumentDetailsResponse.of(document.getId(), "");
        }

        PromptProperties.DetailsPrompt prompts = promptProperties.getDetails();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);
        String mergedPrompt = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);

        String raw = aiTextClient.complete(mergedPrompt);
        String details = raw == null ? "" : raw.trim();

        if (details.isBlank()) {
            document.updateDetails(null);
            return DocumentDetailsResponse.of(document.getId(), "");
        }

        document.updateDetails(details);
        return DocumentDetailsResponse.of(document.getId(), details);
    }

    @Transactional(readOnly = true)
    public DocumentDetailsResponse get(Long documentId, CustomUserDetails user) {
        Document document = getOwnedDocument(documentId, user);
        String details = document.getDetails();
        if (details == null || details.isBlank()) {
            return DocumentDetailsResponse.of(document.getId(), "");
        }
        return DocumentDetailsResponse.of(document.getId(), details.trim());
    }

    private Document getOwnedDocument(Long documentId, CustomUserDetails user) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return doc;
    }
}
