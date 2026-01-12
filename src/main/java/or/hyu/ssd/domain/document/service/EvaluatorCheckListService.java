package or.hyu.ssd.domain.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.ai.util.AiTextClient;
import or.hyu.ssd.domain.ai.util.PromptComposer;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorCheckListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.EvaluatorCheckListResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorCheckList;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.PromptProperties;
import or.hyu.ssd.global.util.AiResponseUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EvaluatorCheckListService {

    private final EvaluatorCheckListRepository evaluatorCheckListRepository;
    private final DocumentRepository documentRepository;
    private final PromptProperties promptProperties;
    private final AiTextClient aiTextClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluatorCheckListResponse generate(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);

        // 기존 평가자 체크리스트를 모두 삭제 후 새로 생성한다 (AI가 매번 최신 판단을 내려주기 때문).
        evaluatorCheckListRepository.deleteAllByDocument(doc);

        // 본문이 없으면 프롬프트를 만들 수 없으므로 비어 있는 체크리스트를 반환한다.
        String content = doc.getContent();
        if (content == null || content.isBlank()) {
            return EvaluatorCheckListResponse.of(doc.getId(), List.of());
        }

        // 1) 프롬프트 구성: 시스템/유저 프롬프트를 합쳐 AI에게 전달할 최종 프롬프트를 만든다.
        PromptProperties.EvaluatorChecklistPrompt prompts = promptProperties.getEvaluatorChecklist();
        String systemPrompt = prompts.getSystem();
        String userPrompt = String.format(prompts.getUser(), content);
        String mergedPrompt = PromptComposer.mergeSystemUser(systemPrompt, userPrompt);

        // 2) AI 호출 후 JSON 배열만 추출한다. (```json ... ``` 등 포맷팅을 제거)
        String raw = aiTextClient.complete(mergedPrompt);
        log.info("[평가자 체크리스트 RAW 응답] raw={}", raw);

        String json = AiResponseUtil.extractJsonArray(raw);
        log.info("[평가자 체크리스트 추출 JSON] json={}", json);

        List<AiChecklistItem> items = parseItems(json);
        if (items.isEmpty()) {
            log.warn("[평가자 체크리스트 파싱 결과 없음] documentId={}", documentId);
            throw new UserExceptionHandler(ErrorCode.EVALUATOR_CHECKLIST_PARSE_ERROR);
        }

        // 3) AI 결과를 중복 없이 순서대로 병합한다.
        //    동일한 content가 여러 번 오더라도 첫 번째 판단만 반영하고, checked가 null이면 기본값(false)으로 처리한다.
        LinkedHashMap<String, Boolean> merged = new LinkedHashMap<>();
        for (AiChecklistItem item : items) {
            if (item == null) continue;
            String text = item.content();
            if (text == null) continue;
            String trimmed = text.trim();
            if (trimmed.isEmpty()) continue;
            merged.putIfAbsent(trimmed, item.checked() != null && item.checked());
        }

        // 키밸류 형식의 해쉬맵에 저장된 content와 checked를 엔티티에 반영
        List<EvaluatorCheckList> toCreate = merged.entrySet().stream()
                .map(e -> EvaluatorCheckList.of(e.getKey(), e.getValue(), doc))
                .collect(Collectors.toList());

        List<EvaluatorCheckList> saved = toCreate.isEmpty() ? List.of() : evaluatorCheckListRepository.saveAll(toCreate);

        List<EvaluatorCheckListItemResponse> responses = saved.stream()
                .map(EvaluatorCheckListItemResponse::of)
                .collect(Collectors.toList());

        return EvaluatorCheckListResponse.of(doc.getId(), responses);
    }

    @Transactional(readOnly = true)
    public EvaluatorCheckListResponse list(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, user);

        List<EvaluatorCheckList> items = evaluatorCheckListRepository.findAllByDocumentOrderByIdAsc(doc);
        List<EvaluatorCheckListItemResponse> responses = items.stream()
                .map(EvaluatorCheckListItemResponse::of)
                .collect(Collectors.toList());
        return EvaluatorCheckListResponse.of(doc.getId(), responses);
    }

    // String 타입의 JSON응 답을 JSON에 맞도록 파싱해서 반환합니다 -> 파싱에 실패하면 빈 리스트를 응답
    private List<AiChecklistItem> parseItems(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<AiChecklistItem>>() {});
        } catch (Exception e) {
            log.warn("[평가자 체크리스트 JSON 파싱 실패] json={}, error={}", json, e.getMessage());
            throw new UserExceptionHandler(ErrorCode.EVALUATOR_CHECKLIST_PARSE_ERROR);
        }
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

    private record AiChecklistItem(String content, Boolean checked) {}
}
