package or.hyu.ssd.domain.document.controller.dto;

public record UpdateFolderResponse(Long id) {
    public static UpdateFolderResponse of(Long id) {
        return new UpdateFolderResponse(id);
    }
}
