package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.CreateFolderResult;

public record CreateFolderResponse(Long id) {
    public static CreateFolderResponse from(CreateFolderResult result) {
        return new CreateFolderResponse(result.id());
    }
}
