package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import or.hyu.ssd.domain.document.usecase.command.CreateDocumentCommentCommand;

public record DocumentCommentRequest(
        @NotNull @Min(1) Integer blockId,
        @NotBlank String comment
) {
    public CreateDocumentCommentCommand toCommand() {
        return new CreateDocumentCommentCommand(blockId, comment);
    }
}
