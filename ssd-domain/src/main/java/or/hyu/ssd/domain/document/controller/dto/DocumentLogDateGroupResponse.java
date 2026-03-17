package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record DocumentLogDateGroupResponse(
        String savedDate,
        List<DocumentLogItemResponse> logs
) {
    public static DocumentLogDateGroupResponse of(String savedDate, List<DocumentLogItemResponse> logs) {
        return new DocumentLogDateGroupResponse(savedDate, logs == null ? List.of() : logs);
    }
}
