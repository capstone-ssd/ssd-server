package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.DocumentLog;

import java.util.List;
import java.util.stream.Collectors;

public record DocumentLogResponse(
        Long documentId,
        List<DocumentLogItemResponse> logs
) {
    public static DocumentLogResponse of(Long documentId, List<DocumentLog> logs) {
        List<DocumentLogItemResponse> items = logs.stream()
                .map(DocumentLogItemResponse::of)
                .collect(Collectors.toList());
        return new DocumentLogResponse(documentId, items);
    }
}
