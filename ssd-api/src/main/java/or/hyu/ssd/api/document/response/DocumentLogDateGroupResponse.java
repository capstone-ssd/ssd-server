package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.DocumentLogDateGroupResult;

import java.util.List;

public record DocumentLogDateGroupResponse(
        String savedDate,
        List<DocumentLogItemResponse> logs
) {
    public static DocumentLogDateGroupResponse from(DocumentLogDateGroupResult result) {
        return new DocumentLogDateGroupResponse(
                result.savedDate(),
                result.logs().stream().map(DocumentLogItemResponse::from).toList()
        );
    }
}
