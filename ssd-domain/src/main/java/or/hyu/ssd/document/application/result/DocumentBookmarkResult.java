package or.hyu.ssd.document.application.result;

public record DocumentBookmarkResult(
        Long id,
        boolean bookmark
) {
    public static DocumentBookmarkResult of(Long id, boolean bookmark) {
        return new DocumentBookmarkResult(id, bookmark);
    }
}
