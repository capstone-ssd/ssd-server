package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.DocumentLogItemResult;

public record DocumentLogItemResponse(
        String savedTime,
        String editorName,
        String editorEmail,
        int deletedBlockCount,
        int createdBlockCount
) {
    public static DocumentLogItemResponse from(DocumentLogItemResult result) {
        return new DocumentLogItemResponse(
                result.savedTime(),
                result.editorName(),
                result.editorEmail(),
                result.deletedBlockCount(),
                result.createdBlockCount()
        );
    }
}
