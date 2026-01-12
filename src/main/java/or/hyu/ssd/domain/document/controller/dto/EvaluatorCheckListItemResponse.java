package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.EvaluatorCheckList;

public record EvaluatorCheckListItemResponse(
        Long id,
        String content,
        boolean checked
) {
    public static EvaluatorCheckListItemResponse of(EvaluatorCheckList entity) {
        return new EvaluatorCheckListItemResponse(
                entity.getId(),
                entity.getContent(),
                entity.isChecked()
        );
    }
}
