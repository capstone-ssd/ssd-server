package or.hyu.ssd.document.application.command;

public record CreateDocumentCommentCommand(
        Integer blockId,
        String comment
) {
}
