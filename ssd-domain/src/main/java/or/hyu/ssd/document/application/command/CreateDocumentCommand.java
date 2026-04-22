package or.hyu.ssd.document.application.command;

import or.hyu.ssd.document.domain.model.DocumentPurpose;

import java.util.List;

public record CreateDocumentCommand(
        String title,
        String text,
        List<DocumentBlockCommand> blocks,
        Long folderId,
        DocumentPurpose purpose
) {
}
