package or.hyu.ssd.domain.document.usecase.result;

public record DocumentBookmarkResult(
        Long id,
        boolean bookmark
) {
    public static DocumentBookmarkResult of(Long id, boolean bookmark) {
        return new DocumentBookmarkResult(id, bookmark);
    }
}
