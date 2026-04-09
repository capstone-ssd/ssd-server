package or.hyu.ssd.domain.document.service.support;

public record DocumentImageUploadPart(
        String blobKey,
        Integer blockId,
        String originalFilename,
        String contentType,
        byte[] bytes
) {
}
