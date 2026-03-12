package or.hyu.ssd.domain.document.controller.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ExternalAiDocumentCheckResponse(
        Long documentId,
        List<Integer> changedBlockIds,
        Map<String, Boolean> checkList
) {
    public static ExternalAiDocumentCheckResponse of(
            Long documentId,
            List<Integer> changedBlockIds,
            Map<String, Boolean> checkList
    ) {
        return new ExternalAiDocumentCheckResponse(
                documentId,
                changedBlockIds == null ? List.of() : List.copyOf(changedBlockIds),
                checkList == null ? Map.of() : new LinkedHashMap<>(checkList)
        );
    }
}
