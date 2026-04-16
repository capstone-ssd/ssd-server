package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.DocumentBookmarkResult;

public record DocumentBookmarkResponse(
        Long id,
        boolean bookmark
) {
    public static DocumentBookmarkResponse from(DocumentBookmarkResult result) {
        return new DocumentBookmarkResponse(result.id(), result.bookmark());
    }
}
