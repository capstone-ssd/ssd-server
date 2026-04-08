package or.hyu.ssd.domain.document.usecase.command;

public record CreateEvaluatorReviewCommand(
        Integer feasibility,
        Integer differentiation,
        Integer financial,
        String comment
) {
}
