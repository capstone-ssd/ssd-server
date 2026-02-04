package or.hyu.ssd.domain.document.controller.dto;

public record UpdateFolderRequest(
        String name,
        String color,
        Long parentId
) {}
