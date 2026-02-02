package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.ai.util.AiTextClient;
import or.hyu.ssd.domain.ai.util.PromptComposer;
import or.hyu.ssd.domain.document.controller.dto.DocumentEvaluationResponse;
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
public class EvaluationService {

    private final DocumentRepository documentRepository;
    private final PromptProperties promptProperties;
    private final AiTextClient aiTextClient;

    public DocumentEvaluationResponse generate(Long documentId, CustomUserDetails user) {
        Document document = getOwnedDocument(documentId, user);

        // 기존 평가 삭제 후 재생성
        document.updateEvaluation(null);

        String content = document.getContent();
        if (content == null || content.isBlank()) {
            return DocumentEvaluationResponse.of(document.getId(), "");
        }

        PromptProperties.EvaluationPrompt prompts = promptProperties.getEvaluation();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);
        String mergedPrompt = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);

        String raw = aiTextClient.complete(mergedPrompt);
        String evaluation = raw == null ? "" : raw.trim();

        if (evaluation.isBlank()) {
            document.updateEvaluation(null);
            return DocumentEvaluationResponse.of(document.getId(), "");
        }

        document.updateEvaluation(evaluation);
        return DocumentEvaluationResponse.of(document.getId(), evaluation);
    }

    @Transactional(readOnly = true)
    public DocumentEvaluationResponse get(Long documentId, CustomUserDetails user) {
        Document document = getOwnedDocument(documentId, user);
        String evaluation = document.getEvaluation();
        if (evaluation == null || evaluation.isBlank()) {
            return DocumentEvaluationResponse.of(document.getId(), "");
        }
        return DocumentEvaluationResponse.of(document.getId(), evaluation.trim());
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
