package or.hyu.ssd.domain.document.usecase.result;

public record UpdateFolderResult(Long id) {
    public static UpdateFolderResult of(Long id) {
        return new UpdateFolderResult(id);
    }
}
