package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.repository.projection.DocumentSearchSuggestionProjection;

import java.util.List;

public interface DocumentJpaRepositoryCustom {

    List<DocumentSearchSuggestionProjection> findSearchSuggestions(Long memberId, String keyword, int limit, double threshold);
}
