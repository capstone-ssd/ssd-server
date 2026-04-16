package or.hyu.ssd.document.application.result;

public record UpdateFolderResult(Long id) {
    public static UpdateFolderResult of(Long id) {
        return new UpdateFolderResult(id);
    }
}
