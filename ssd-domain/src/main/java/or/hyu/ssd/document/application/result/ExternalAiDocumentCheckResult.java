package or.hyu.ssd.document.application.result;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ExternalAiDocumentCheckResult(
        Long documentId,
        List<Integer> changedBlockIds,
        Map<String, Boolean> checkList
) {
    public static ExternalAiDocumentCheckResult of(Long documentId, List<Integer> changedBlockIds, Map<String, Boolean> checkList) {
        return new ExternalAiDocumentCheckResult(
                documentId,
                changedBlockIds == null ? List.of() : List.copyOf(changedBlockIds),
                checkList == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(checkList))
        );
    }
}
