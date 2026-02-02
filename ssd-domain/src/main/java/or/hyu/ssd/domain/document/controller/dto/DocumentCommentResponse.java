package or.hyu.ssd.domain.document.controller.dto;

public record DocumentCommentResponse(Long id) {

    public static DocumentCommentResponse of(Long id) {
        return new DocumentCommentResponse(id);
    }
}
