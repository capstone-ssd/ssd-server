package or.hyu.ssd.document.application.support;

import or.hyu.ssd.document.application.command.DocumentBlockCommand;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DocumentImageResolver {

    Map<String, DocumentImageUploadPart> indexUploadParts(List<DocumentImageUploadPart> imageUploadParts);

    String resolveImageUrl(
            DocumentBlockCommand block,
            int blockId,
            Map<String, DocumentImageUploadPart> imageUploadsByBlobKey,
            Long memberId
    );

    void validateAllMapped(Map<String, DocumentImageUploadPart> imageUploadsByBlobKey, Set<String> resolvedBlobKeys);

    String replaceBlobKeys(String text, Map<String, String> uploadedUrlsByBlobKey);
}
