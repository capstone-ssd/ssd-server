package or.hyu.ssd.domain.document.usecase.command;

public record UpdateFolderCommand(
        String name,
        String color,
        Long parentId
) {
}
