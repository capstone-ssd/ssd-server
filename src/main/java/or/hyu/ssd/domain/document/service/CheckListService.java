package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CheckListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.GenerateChecklistResponse;
import or.hyu.ssd.domain.document.entity.CheckList;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.domain.ai.util.AiTextClient;
import or.hyu.ssd.domain.ai.util.PromptComposer;
import or.hyu.ssd.global.config.properties.PromptProperties;
import or.hyu.ssd.global.util.AiResponseUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CheckListService {

    /**
     * 1. 체크리스트 체크 관련 동시성 여부 - 낙관적 락을 통해 구현
     * 2. 체크리스트 등록 API 기존의 체크리스트가 있을때 어떻게 할 것인가?
     *  - 이미 있으면 기존의 것들 전부 삭제하고 다시 새로운 데이터로 채우는 로직이 필요함
     * */

    private final CheckListRepository checkListRepository;
    private final DocumentRepository documentRepository;
    private final AiTextClient aiTextClient;
    private final PromptProperties promptProperties;

    @Transactional(readOnly = true)
    public List<CheckListItemResponse> listByDocument(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);
        return checkListRepository.findAllByDocument(doc).stream()
                .map(CheckListItemResponse::of)
                .collect(Collectors.toList());
    }

    public CheckListItemResponse toggleChecked(Long checkListId, CustomUserDetails user) {
        CheckList entity = getOwnedCheckList(checkListId, user);
        entity.updateChecked(!entity.isChecked());
        checkListRepository.flush();
        return CheckListItemResponse.of(entity);
    }

    public void delete(Long checkListId, CustomUserDetails user) {
        CheckList entity = getOwnedCheckList(checkListId, user);
        checkListRepository.delete(entity);
    }

    public GenerateChecklistResponse generate(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);

        String content = doc.getContent();
        if (content == null || content.isBlank()) {
            checkListRepository.deleteAllByDocument(doc);
            return GenerateChecklistResponse.of(doc.getId(), List.of());
        }

        PromptProperties.ChecklistPrompt prompts = promptProperties.getChecklist();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);

        String mergedPrompt = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);
        String raw = aiTextClient.complete(mergedPrompt);

        String json = AiResponseUtil.extractJsonArray(raw);
        List<String> rawItems = AiResponseUtil.parseStringArray(json);

        // 중복 제거 + 순서 유지
        java.util.LinkedHashSet<String> targetSet = new java.util.LinkedHashSet<>(rawItems);
        List<String> targetItems = new java.util.ArrayList<>(targetSet);

        // 기존 항목 조회
        List<CheckList> existing = checkListRepository.findAllByDocument(doc);
        java.util.Map<String, CheckList> existingByContent = existing.stream()
                .collect(java.util.stream.Collectors.toMap(CheckList::getContent, java.util.function.Function.identity(), (a,b)->a));

        // 삭제 목록: 타깃에 없는 기존 항목
        List<CheckList> toDelete = existing.stream()
                .filter(e -> !targetSet.contains(e.getContent()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            checkListRepository.deleteAll(toDelete);
        }

        // 생성 목록: 기존에 없는 신규 항목만
        List<CheckList> toCreate = targetItems.stream()
                .filter(s -> !existingByContent.containsKey(s))
                .map(s -> CheckList.of(s, doc))
                .collect(Collectors.toList());

        List<CheckList> created = toCreate.isEmpty() ? java.util.List.of() : checkListRepository.saveAll(toCreate);
        java.util.Map<String, CheckList> createdByContent = created.stream()
                .collect(java.util.stream.Collectors.toMap(CheckList::getContent, java.util.function.Function.identity()));

        // 응답은 targetItems 순서를 따르고, 기존 항목은 체크 상태 보존
        List<CheckListItemResponse> responses = new java.util.ArrayList<>();
        for (String s : targetItems) {
            CheckList e = existingByContent.get(s);
            if (e == null) e = createdByContent.get(s);
            if (e != null) responses.add(CheckListItemResponse.of(e));
        }

        return GenerateChecklistResponse.of(doc.getId(), responses);
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

    private CheckList getOwnedCheckList(Long checkListId, CustomUserDetails user) {
        CheckList cl = checkListRepository.findById(checkListId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.CHECKLIST_NOT_FOUND));
        Document doc = cl.getDocument();
        if (doc == null || doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.CHECKLIST_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.CHECKLIST_FORBIDDEN);
        }
        return cl;
    }
}
