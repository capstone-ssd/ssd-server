package or.hyu.ssd.api.document.request;

import jakarta.validation.constraints.NotBlank;
import or.hyu.ssd.domain.document.usecase.command.UpdateDocumentCommentCommand;

public record DocumentCommentUpdateRequest(
        @NotBlank String comment
) {
    public UpdateDocumentCommentCommand toCommand() {
        return new UpdateDocumentCommentCommand(comment);
    }
}
