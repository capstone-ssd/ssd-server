package or.hyu.ssd.domain.document.controller.dto;

import java.util.Map;
import java.util.LinkedHashMap;

public record ExternalAiBlockCheckResponse(
        Integer blockId,
        Map<String, Boolean> checkList
) {
    public static ExternalAiBlockCheckResponse of(Integer blockId, Map<String, Boolean> checkList) {
        return new ExternalAiBlockCheckResponse(
                blockId,
                checkList == null ? Map.of() : new LinkedHashMap<>(checkList)
        );
    }
}
