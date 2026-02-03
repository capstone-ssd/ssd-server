package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record EvaluatorReviewResponse(
        EvaluatorReviewSummaryResponse summary,
        List<EvaluatorReviewItemResponse> reviews
) {
    public static EvaluatorReviewResponse of(EvaluatorReviewSummaryResponse summary, List<EvaluatorReviewItemResponse> reviews) {
        List<EvaluatorReviewItemResponse> safe = reviews == null ? List.of() : reviews;
        return new EvaluatorReviewResponse(summary, safe);
    }
}
