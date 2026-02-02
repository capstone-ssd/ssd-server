package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record GenerateChecklistResponse(
        Long documentId,
        List<CheckListItemResponse> items
) {
    public static GenerateChecklistResponse of(Long documentId, List<CheckListItemResponse> items) {
        return new GenerateChecklistResponse(documentId, items);
    }
}

