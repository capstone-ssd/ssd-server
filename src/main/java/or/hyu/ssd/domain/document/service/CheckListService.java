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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CheckListService {

    /**
     * 1. 체크리스트 체크 동시성: 낙관적 락(@Version)으로 충돌 감지
     * 2. 체크리스트 등록(generate): 기존 항목은 유지하고, AI가 제안한 항목 중 신규 content만 추가
     */

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


    /**
     * 체크리스트 생성 및 저장 API
     * */
    public GenerateChecklistResponse generate(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);

        String content = doc.getContent();
        if (content == null || content.isBlank()) {
            checkListRepository.deleteAllByDocument(doc);
            return GenerateChecklistResponse.of(doc.getId(), List.of());
        }

        // 1. yml에서 프롬프트 조회
        PromptProperties.ChecklistPrompt prompts = promptProperties.getChecklist();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);

        // 2. 프롬프트 조합
        String mergedPrompt = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);
        String raw = aiTextClient.complete(mergedPrompt);

        String json = AiResponseUtil.extractJsonArray(raw);
        List<String> rawItems = AiResponseUtil.parseStringArray(json);

        // 3. 중복 제거 + 순서 유지
        LinkedHashSet<String> targetSet = new java.util.LinkedHashSet<>(rawItems);
        List<String> targetItems = new java.util.ArrayList<>(targetSet);

        // 4. 기존 항목 조회
        List<CheckList> existing = checkListRepository.findAllByDocument(doc);
        Set<String> existingContents = existing.stream()
                .map(CheckList::getContent)
                .collect(java.util.stream.Collectors.toSet());

        // 5. 생성 목록: 기존에 없는 신규 항목만 추가
        List<CheckList> toCreate = targetItems.stream()
                .filter(s -> !existingContents.contains(s))
                .map(s -> CheckList.of(s, doc))
                .collect(Collectors.toList());

        List<CheckList> created = toCreate.isEmpty() ? List.of() : checkListRepository.saveAll(toCreate);

        // 6. 응답은 새로 추가된 항목만 반환
        List<CheckListItemResponse> createdResponses = created.stream()
                .map(CheckListItemResponse::of)
                .collect(Collectors.toList());

        return GenerateChecklistResponse.of(doc.getId(), createdResponses);
    }





    /**
     * 사업계획서의 작성자를 확인하는 헬퍼 메서드
     * */
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

    /**
     * 체크리스트 작성자를 확인하는 헬퍼 메서드
     * */
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
