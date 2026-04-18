package or.hyu.ssd.document.application.result;

public record CreateFolderResult(Long id) {
    public static CreateFolderResult of(Long id) {
        return new CreateFolderResult(id);
    }
}
