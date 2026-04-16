package or.hyu.ssd.document.application.command;

public record CreateFolderCommand(
        String name,
        String color,
        Long parentId
) {
}
