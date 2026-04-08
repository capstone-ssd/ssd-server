package or.hyu.ssd.domain.document.usecase.command;

public record UpdateEvaluatorReviewCommand(
        Integer feasibility,
        Integer differentiation,
        Integer financial,
        String comment
) {
}
