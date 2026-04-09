package or.hyu.ssd.domain.document.usecase.result;

public record CreateFolderResult(Long id) {
    public static CreateFolderResult of(Long id) {
        return new CreateFolderResult(id);
    }
}
