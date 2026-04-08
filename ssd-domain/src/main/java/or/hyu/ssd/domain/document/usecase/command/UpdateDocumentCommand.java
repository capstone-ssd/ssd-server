package or.hyu.ssd.domain.document.usecase.command;

import java.util.List;

public record UpdateDocumentCommand(
        String title,
        String text,
        List<DocumentBlockCommand> blocks
) {
}
