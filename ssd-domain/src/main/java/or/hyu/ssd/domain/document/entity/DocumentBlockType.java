package or.hyu.ssd.domain.document.entity;

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
