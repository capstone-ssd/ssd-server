package or.hyu.ssd.domain.document.controller.dto;

public record EvaluatorReviewIdResponse(Long id) {
    public static EvaluatorReviewIdResponse of(Long id) {
        return new EvaluatorReviewIdResponse(id);
    }
}
