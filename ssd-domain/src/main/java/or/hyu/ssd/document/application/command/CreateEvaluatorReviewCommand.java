package or.hyu.ssd.document.application.command;

public record CreateEvaluatorReviewCommand(
        Integer feasibility,
        Integer differentiation,
        Integer financial,
        String comment
) {
}
