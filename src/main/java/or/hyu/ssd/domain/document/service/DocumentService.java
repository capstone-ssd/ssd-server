package or.hyu.ssd.domain.document.service;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CheckListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.GenerateChecklistResponse;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.entity.CheckList;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.PromptProperties;
import or.hyu.ssd.domain.ai.util.AiTextClient;
import or.hyu.ssd.global.util.AiResponseUtil;
import or.hyu.ssd.domain.ai.util.PromptComposer;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CheckListRepository checkListRepository;
    private final AiTextClient aiTextClient;
    private final PromptProperties promptProperties;

    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req) {

        Document doc = Document.of(req.title(), req.content(), false, user.getMember());

        Document saved = documentRepository.save(doc);
        return CreateDocumentResponse.of(saved.getId());
    }

    public UpdateDocumentResponse updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentRequest req) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        doc.updateIfPresent(req.title(), req.content(), req.summary(), req.details(), req.bookmark());

        return UpdateDocumentResponse.of(doc.getId());
    }

    public void deleteDocument(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        checkListRepository.deleteAllByDocument(doc);
        documentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public GetDocumentResponse getDocument(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        return GetDocumentResponse.of(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentListItemResponse> listDocuments(CustomUserDetails user, DocumentSort sortOption) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }

        Sort sort = switch (sortOption) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"));
            case NAME -> Sort.by(Sort.Order.asc("title"));
            case MODIFIED -> Sort.by(Sort.Order.desc("updatedAt"));
        };

        return documentRepository.findAllByMember_Id(user.getMember().getId(), sort)
                .stream()
                .map(DocumentListItemResponse::of)
                .collect(Collectors.toList());
    }






    public GenerateChecklistResponse generateChecklist(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        String content = doc.getContent();
        if (content == null || content.isBlank()) {
            // 빈 본문이면 기존 체크리스트만 삭제하고 빈 결과 반환
            checkListRepository.deleteAllByDocument(doc);
            return GenerateChecklistResponse.of(doc.getId(), List.of());
        }

        PromptProperties.ChecklistPrompt prompts = promptProperties.getChecklist();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);
        

        String mergedPrompt = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);
        String raw = aiTextClient.complete(mergedPrompt);

        // 응답 파싱은 유틸로 캡슐화
        String json = AiResponseUtil.extractJsonArray(raw);
        List<String> items = AiResponseUtil.parseStringArray(json);

        checkListRepository.deleteAllByDocument(doc);

        // 엔티티 생성은 정적 팩토리로 위임
        List<CheckList> toSave = items.stream()
                .map(it -> CheckList.of(it, doc))
                .collect(Collectors.toList());

        List<CheckList> saved = checkListRepository.saveAll(toSave);
        List<CheckListItemResponse> responses = saved.stream()
                .map(CheckListItemResponse::of)
                .collect(Collectors.toList());

        return GenerateChecklistResponse.of(doc.getId(), responses);
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
    }
}
