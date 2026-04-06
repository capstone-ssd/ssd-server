package or.hyu.ssd.domain.document.controller.dto;

public record DocumentImageUploadPart(
        String blobKey,
        Integer blockId,
        String originalFilename,
        String contentType,
        byte[] bytes
) {
}
