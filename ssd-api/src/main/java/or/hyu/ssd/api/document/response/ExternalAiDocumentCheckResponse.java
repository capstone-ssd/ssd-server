package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.ExternalAiDocumentCheckResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ExternalAiDocumentCheckResponse(
        Long documentId,
        List<Integer> changedBlockIds,
        Map<String, Boolean> checkList
) {
    public static ExternalAiDocumentCheckResponse from(ExternalAiDocumentCheckResult result) {
        return new ExternalAiDocumentCheckResponse(
                result.documentId(),
                result.changedBlockIds() == null ? List.of() : List.copyOf(result.changedBlockIds()),
                result.checkList() == null ? Map.of() : new LinkedHashMap<>(result.checkList())
        );
    }
}
