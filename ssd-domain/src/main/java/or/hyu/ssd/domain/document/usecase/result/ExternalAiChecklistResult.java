package or.hyu.ssd.domain.document.usecase.result;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExternalAiChecklistResult(
        Long documentId,
        Map<String, Boolean> checkList
) {
    public static ExternalAiChecklistResult of(Long documentId, Map<String, Boolean> checkList) {
        return new ExternalAiChecklistResult(documentId, checkList == null ? Map.of() : new LinkedHashMap<>(checkList));
    }
}
