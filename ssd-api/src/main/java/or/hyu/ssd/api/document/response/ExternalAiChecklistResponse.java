package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.ExternalAiChecklistResult;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExternalAiChecklistResponse(
        Long documentId,
        Map<String, Boolean> checkList
) {
    public static ExternalAiChecklistResponse from(ExternalAiChecklistResult result) {
        return new ExternalAiChecklistResponse(
                result.documentId(),
                result.checkList() == null ? Map.of() : new LinkedHashMap<>(result.checkList())
        );
    }
}
