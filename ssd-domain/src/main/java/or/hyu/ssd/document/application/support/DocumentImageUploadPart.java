package or.hyu.ssd.document.application.support;

public record DocumentImageUploadPart(
        String blobKey,
        Integer blockId,
        String originalFilename,
        String contentType,
        byte[] bytes
) {
}
