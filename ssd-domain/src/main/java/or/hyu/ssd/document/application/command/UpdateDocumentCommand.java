package or.hyu.ssd.document.application.command;

import java.util.List;

public record UpdateDocumentCommand(
        String title,
        String text,
        List<DocumentBlockCommand> blocks
) {
}
