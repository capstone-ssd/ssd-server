package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.UpdateFolderResult;

public record UpdateFolderResponse(Long id) {
    public static UpdateFolderResponse from(UpdateFolderResult result) {
        return new UpdateFolderResponse(result.id());
    }
}
