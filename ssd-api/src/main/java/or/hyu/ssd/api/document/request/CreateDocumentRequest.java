package or.hyu.ssd.api.document.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import or.hyu.ssd.document.application.command.CreateDocumentCommand;

import java.util.List;

public record CreateDocumentRequest(
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String text,
        List<@NotNull(message = "문단 블록 항목은 null일 수 없습니다") @Valid CreateDocumentBlockRequest> paragraphs,
        @PositiveOrZero(message = "폴더 ID는 0 이상이어야 합니다")
        Long folderId
) {
    public CreateDocumentCommand toCommand() {
        return new CreateDocumentCommand(
                title,
                text,
                paragraphs == null ? null : paragraphs.stream().map(CreateDocumentBlockRequest::toCommand).toList(),
                folderId
        );
    }
}
