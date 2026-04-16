package or.hyu.ssd.api.document.request;

import jakarta.validation.constraints.PositiveOrZero;
import or.hyu.ssd.document.application.command.UpdateFolderCommand;

public record UpdateFolderRequest(
        String name,
        String color,
        @PositiveOrZero(message = "상위 폴더 ID는 0 이상이어야 합니다")
        Long parentId
) {
    public UpdateFolderCommand toCommand() {
        return new UpdateFolderCommand(name, color, parentId);
    }
}
