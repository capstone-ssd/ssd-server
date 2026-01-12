package or.hyu.ssd.domain.document.controller.dto;

public record DocumentEvaluationResponse(
        Long documentId,
        String evaluation
) {
    public static DocumentEvaluationResponse of(Long documentId, String evaluation) {
        String body = evaluation == null ? "" : evaluation.trim();
        return new DocumentEvaluationResponse(documentId, body);
    }
}
