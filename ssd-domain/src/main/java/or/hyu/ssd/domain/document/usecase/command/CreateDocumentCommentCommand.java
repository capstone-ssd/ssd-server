package or.hyu.ssd.domain.document.usecase.command;

public record CreateDocumentCommentCommand(
        Integer blockId,
        String comment
) {
}
