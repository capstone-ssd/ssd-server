package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record DocumentLogResponse(
        Long documentId,
        List<DocumentLogDateGroupResponse> records
) {
    public static DocumentLogResponse of(Long documentId, List<DocumentLogDateGroupResponse> records) {
        return new DocumentLogResponse(documentId, records == null ? List.of() : records);
    }
}
