package or.hyu.ssd.domain.document.controller.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExternalAiChecklistResponse(
        Long documentId,
        Map<String, Boolean> checkList
) {
    public static ExternalAiChecklistResponse of(Long documentId, Map<String, Boolean> checkList) {
        return new ExternalAiChecklistResponse(
                documentId,
                checkList == null ? Map.of() : new LinkedHashMap<>(checkList)
        );
    }
}
