package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.CreateFolderResult;

public record CreateFolderResponse(Long id) {
    public static CreateFolderResponse from(CreateFolderResult result) {
        return new CreateFolderResponse(result.id());
    }
}
