package or.hyu.ssd.document.domain.model;

public enum DocumentBlockType {
    PARAGRAPH,
    IMAGE;

    public boolean isParagraph() {
        return this == PARAGRAPH;
    }

    public boolean isImage() {
        return this == IMAGE;
    }
}
