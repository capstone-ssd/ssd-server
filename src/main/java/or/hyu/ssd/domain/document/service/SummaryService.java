package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.ai.util.AiTextClient;
import or.hyu.ssd.domain.ai.util.PromptComposer;
import or.hyu.ssd.domain.document.controller.dto.ThreeLineSummaryResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.PromptProperties;
import or.hyu.ssd.global.util.AiResponseUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SummaryService {

    private final DocumentRepository documentRepository;
    private final PromptProperties promptProperties;
    private final AiTextClient aiTextClient;

    public ThreeLineSummaryResponse generate(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);
        String content = doc.getContent();

        if (content == null || content.isBlank()) {
            doc.updateIfPresent(null, null, null, null, null);
            return ThreeLineSummaryResponse.of(doc.getId(), List.of());
        }

        PromptProperties.SummaryPrompt prompts = promptProperties.getSummary();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);
        String merged = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);

        String raw = aiTextClient.complete(merged);
        String json = AiResponseUtil.extractJsonArray(raw);
        List<String> lines = AiResponseUtil.parseStringArray(json);

        List<String> three = new ArrayList<>();
        for (String line : lines) {
            if (line == null) continue;
            String s = line.trim();
            if (!s.isEmpty()) three.add(s);
            if (three.size() == 3) break;
        }

        String summary = String.join("\n", three);
        doc.updateIfPresent(null, null, summary, null, null);
        return ThreeLineSummaryResponse.of(doc.getId(), three);
    }

    @Transactional(readOnly = true)
    public ThreeLineSummaryResponse get(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);
        String summary = doc.getSummary();
        if (summary == null || summary.isBlank()) {
            return ThreeLineSummaryResponse.of(doc.getId(), List.of());
        }
        String[] arr = summary.split("\n");
        List<String> lines = new ArrayList<>();
        for (String a : arr) {
            if (a != null && !a.isBlank()) lines.add(a.trim());
        }
        return ThreeLineSummaryResponse.of(doc.getId(), lines);
    }

    public void delete(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);
        doc.updateIfPresent(null, null, null, null, null);
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

