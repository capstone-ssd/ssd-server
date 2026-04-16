package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.CreateFolderResult;

public record CreateFolderResponse(Long id) {
    public static CreateFolderResponse from(CreateFolderResult result) {
        return new CreateFolderResponse(result.id());
    }
}
