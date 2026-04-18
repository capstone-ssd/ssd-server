package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.DocumentLogResult;

import java.util.List;

public record DocumentLogResponse(
        Long documentId,
        List<DocumentLogDateGroupResponse> records
) {
    public static DocumentLogResponse from(DocumentLogResult result) {
        return new DocumentLogResponse(
                result.documentId(),
                result.records().stream().map(DocumentLogDateGroupResponse::from).toList()
        );
    }
}
