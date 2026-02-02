package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record EvaluatorCheckListResponse(
        Long documentId,
        List<EvaluatorCheckListItemResponse> items
) {
    public static EvaluatorCheckListResponse of(Long documentId, List<EvaluatorCheckListItemResponse> items) {
        List<EvaluatorCheckListItemResponse> safe = items == null ? List.of() : items;
        return new EvaluatorCheckListResponse(documentId, safe);
    }
}
