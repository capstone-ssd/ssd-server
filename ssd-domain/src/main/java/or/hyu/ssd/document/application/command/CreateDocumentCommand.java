package or.hyu.ssd.document.application.command;

import java.util.List;

public record CreateDocumentCommand(
        String title,
        String text,
        List<DocumentBlockCommand> blocks,
        Long folderId
) {
}
