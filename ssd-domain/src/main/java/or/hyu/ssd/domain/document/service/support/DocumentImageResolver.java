package or.hyu.ssd.domain.document.service.support;

import or.hyu.ssd.domain.document.controller.dto.CreateDocumentParagraphRequest;
import or.hyu.ssd.domain.document.controller.dto.DocumentImageUploadPart;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DocumentImageResolver {

    Map<String, DocumentImageUploadPart> indexUploadParts(List<DocumentImageUploadPart> imageUploadParts);

    String resolveImageUrl(
            CreateDocumentParagraphRequest block,
            int blockId,
            Map<String, DocumentImageUploadPart> imageUploadsByBlobKey,
            Long memberId
    );

    void validateAllMapped(Map<String, DocumentImageUploadPart> imageUploadsByBlobKey, Set<String> resolvedBlobKeys);

    String replaceBlobKeys(String text, Map<String, String> uploadedUrlsByBlobKey);
}
