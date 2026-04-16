package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.UpdateFolderResult;

public record UpdateFolderResponse(Long id) {
    public static UpdateFolderResponse from(UpdateFolderResult result) {
        return new UpdateFolderResponse(result.id());
    }
}
