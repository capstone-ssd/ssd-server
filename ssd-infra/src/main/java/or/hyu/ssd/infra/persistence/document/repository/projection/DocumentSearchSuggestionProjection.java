package or.hyu.ssd.infra.persistence.document.repository.projection;

public interface DocumentSearchSuggestionProjection {
    String getKeyword();

    Double getScore();
}
