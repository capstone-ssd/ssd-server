package or.hyu.ssd.document.application.command;

public record UpdateFolderCommand(
        String name,
        String color,
        Long parentId
) {
}
