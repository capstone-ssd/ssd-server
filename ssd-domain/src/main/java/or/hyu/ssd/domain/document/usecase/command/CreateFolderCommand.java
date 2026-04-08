package or.hyu.ssd.domain.document.usecase.command;

public record CreateFolderCommand(
        String name,
        String color,
        Long parentId
) {
}
