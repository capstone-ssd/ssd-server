package or.hyu.ssd.document.application.support;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.port.DocumentImageStoragePort;
import or.hyu.ssd.document.application.command.DocumentBlockCommand;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentImageResolverImpl implements DocumentImageResolver {

    private final DocumentImageStoragePort documentImageStoragePort;

    @Override
    public Map<String, DocumentImageUploadPart> indexUploadParts(List<DocumentImageUploadPart> imageUploadParts) {
        if (imageUploadParts == null || imageUploadParts.isEmpty()) {
            return Map.of();
        }

        return imageUploadParts.stream()
                .peek(part -> {
                    if (isBlank(part.blobKey())) {
                        throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas의 blobKey는 필수입니다");
                    }
                    if (part.blockId() == null || part.blockId() <= 0) {
                        throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas의 blockId는 1 이상이어야 합니다");
                    }
                    if (part.bytes() == null || part.bytes().length == 0) {
                        throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "이미지 파일이 비어 있습니다");
                    }
                    if (part.contentType() == null || !part.contentType().startsWith("image/")) {
                        throw new DocumentException(ErrorCode.REQUEST_MEDIA_TYPE_NOT_SUPPORTED, "이미지 파일만 업로드할 수 있습니다");
                    }
                })
                .collect(Collectors.toMap(
                        DocumentImageUploadPart::blobKey,
                        part -> part,
                        (left, right) -> {
                            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "중복된 blobKey가 있습니다: " + left.blobKey());
                        }
                ));
    }

    @Override
    public String resolveImageUrl(
            DocumentBlockCommand block,
            int blockId,
            Map<String, DocumentImageUploadPart> imageUploadsByBlobKey,
            Long memberId
    ) {
        String imageUrl = trimOrNull(block.url());
        if (imageUrl != null) {
            return imageUrl;
        }

        String blobKey = trimOrNull(block.blobKey());
        if (blobKey == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "이미지 블록에는 blobKey 또는 url이 필요합니다");
        }

        DocumentImageUploadPart imageUploadPart = imageUploadsByBlobKey.get(blobKey);
        if (imageUploadPart == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "blobKey에 해당하는 이미지 파일이 없습니다: " + blobKey);
        }
        if (imageUploadPart.blockId() != null && imageUploadPart.blockId() != blockId) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas의 blockId와 요청 블록의 blockId가 일치하지 않습니다: " + blobKey);
        }

        String storageKey = buildImageStorageKey(memberId, blockId, imageUploadPart.originalFilename());
        return documentImageStoragePort.upload(storageKey, imageUploadPart.bytes(), imageUploadPart.contentType());
    }

    @Override
    public void validateAllMapped(Map<String, DocumentImageUploadPart> imageUploadsByBlobKey, Set<String> resolvedBlobKeys) {
        if (imageUploadsByBlobKey.isEmpty()) {
            return;
        }

        for (String blobKey : imageUploadsByBlobKey.keySet()) {
            if (!resolvedBlobKeys.contains(blobKey)) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "본문에 없는 blobKey가 imageMetas로 전달되었습니다: " + blobKey);
            }
        }
    }

    @Override
    public String replaceBlobKeys(String text, Map<String, String> uploadedUrlsByBlobKey) {
        if (text == null || text.isBlank() || uploadedUrlsByBlobKey == null || uploadedUrlsByBlobKey.isEmpty()) {
            return text;
        }

        String resolvedText = text;
        for (Map.Entry<String, String> entry : uploadedUrlsByBlobKey.entrySet()) {
            resolvedText = resolvedText.replace(entry.getKey(), entry.getValue());
        }
        return resolvedText;
    }

    private String buildImageStorageKey(Long memberId, int blockId, String originalFilename) {
        String extension = "";
        if (!isBlank(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return "documents/%d/%s-block-%d%s".formatted(memberId, UUID.randomUUID(), blockId, extension);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
