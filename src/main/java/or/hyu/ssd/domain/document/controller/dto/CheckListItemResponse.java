package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.CheckList;

public record CheckListItemResponse(
        Long id,
        String content,
        boolean checked
) {
    public static CheckListItemResponse of(CheckList entity) {
        return new CheckListItemResponse(
                entity.getId(),
                entity.getContent(),
                entity.isChecked()
        );
    }
}

