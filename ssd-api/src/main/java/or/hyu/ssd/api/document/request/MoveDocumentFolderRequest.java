package or.hyu.ssd.api.document.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import or.hyu.ssd.document.application.command.MoveDocumentFolderCommand;

public record MoveDocumentFolderRequest(
        @NotNull(message = "폴더 ID는 필수입니다")
        @PositiveOrZero(message = "폴더 ID는 0 이상이어야 합니다")
        Long folderId
) {
    public MoveDocumentFolderCommand toCommand() {
        return new MoveDocumentFolderCommand(folderId);
    }
}
