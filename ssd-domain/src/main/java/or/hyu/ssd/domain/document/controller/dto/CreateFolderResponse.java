package or.hyu.ssd.domain.document.controller.dto;

public record CreateFolderResponse(Long id) {
    public static CreateFolderResponse of(Long id) {
        return new CreateFolderResponse(id);
    }
}
