package or.hyu.ssd.document.application.event;

public record DocumentSearchIndexEvent(
        Long documentId,
        Action action
) {

    public static DocumentSearchIndexEvent index(Long documentId) {
        return new DocumentSearchIndexEvent(documentId, Action.INDEX);
    }

    public static DocumentSearchIndexEvent delete(Long documentId) {
        return new DocumentSearchIndexEvent(documentId, Action.DELETE);
    }

    public enum Action {
        INDEX,
        DELETE
    }
}
