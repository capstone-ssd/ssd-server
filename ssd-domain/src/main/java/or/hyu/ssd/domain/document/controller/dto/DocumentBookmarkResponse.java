package or.hyu.ssd.domain.document.controller.dto;

public record DocumentBookmarkResponse(
        Long id,
        boolean bookmark
) {
    public static DocumentBookmarkResponse of(Long id, boolean bookmark) {
        return new DocumentBookmarkResponse(id, bookmark);
    }
}

