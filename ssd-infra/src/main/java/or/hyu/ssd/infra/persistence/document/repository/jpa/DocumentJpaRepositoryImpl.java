package or.hyu.ssd.infra.persistence.document.repository.jpa;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.infra.persistence.document.entity.QDocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.repository.projection.DocumentSearchSuggestionProjection;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DocumentJpaRepositoryImpl implements DocumentJpaRepositoryCustom {

    private static final QDocumentJpaEntity document = QDocumentJpaEntity.documentJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DocumentSearchSuggestionProjection> findSearchSuggestions(Long memberId, String keyword, int limit, double threshold) {
        Map<String, Double> candidates = findTitleSuggestions(memberId, keyword).stream()
                .collect(Collectors.toMap(
                        Suggestion::keyword,
                        Suggestion::score,
                        Math::max
                ));

        findKeywordSuggestions(memberId, keyword, threshold).forEach(suggestion -> candidates.merge(
                suggestion.keyword(),
                suggestion.score(),
                Math::max
        ));

        return candidates.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted((left, right) -> {
                    int scoreCompare = Double.compare(right.getValue(), left.getValue());
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    return left.getKey().compareTo(right.getKey());
                })
                .limit(limit)
                .map(entry -> new SuggestionProjection(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<Suggestion> findTitleSuggestions(Long memberId, String keyword) {
        NumberExpression<Double> scoreExpression = Expressions.numberTemplate(
                Double.class,
                "similarity({0}, {1})",
                document.title,
                keyword
        );

        List<Tuple> rows = queryFactory
                .select(document.title, scoreExpression)
                .from(document)
                .where(
                        document.member.id.eq(memberId),
                        document.title.isNotNull(),
                        Expressions.booleanTemplate("{0} % {1}", document.title, keyword)
                )
                .fetch();

        return rows.stream()
                .map(row -> new Suggestion(row.get(document.title), row.get(scoreExpression)))
                .filter(suggestion -> suggestion.keyword() != null && suggestion.score() != null)
                .toList();
    }

    private List<Suggestion> findKeywordSuggestions(Long memberId, String keyword, double threshold) {
        List<String> keywordRows = queryFactory
                .select(document.keywords)
                .from(document)
                .where(
                        document.member.id.eq(memberId),
                        document.keywords.isNotNull(),
                        Expressions.booleanTemplate("{0} % {1}", document.keywords, keyword)
                )
                .fetch();

        return keywordRows.stream()
                .flatMap(row -> Arrays.stream(row.split(",")))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .map(value -> new Suggestion(value, trigramSimilarity(value, keyword)))
                .filter(suggestion -> suggestion.score() >= threshold)
                .toList();
    }

    private double trigramSimilarity(String left, String right) {
        Set<String> leftTrigrams = trigrams(left);
        Set<String> rightTrigrams = trigrams(right);
        if (leftTrigrams.isEmpty() && rightTrigrams.isEmpty()) {
            return 1.0;
        }
        if (leftTrigrams.isEmpty() || rightTrigrams.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new LinkedHashSet<>(leftTrigrams);
        intersection.retainAll(rightTrigrams);
        return (double) intersection.size() / Math.max(leftTrigrams.size(), rightTrigrams.size());
    }

    private Set<String> trigrams(String value) {
        String normalized = "  " + value.toLowerCase(Locale.ROOT).trim() + " ";
        Set<String> trigrams = new LinkedHashSet<>();
        for (int index = 0; index <= normalized.length() - 3; index++) {
            trigrams.add(normalized.substring(index, index + 3));
        }
        return trigrams;
    }

    private record Suggestion(String keyword, Double score) {
    }

    private record SuggestionProjection(String keyword, Double score) implements DocumentSearchSuggestionProjection {
        @Override
        public String getKeyword() {
            return keyword;
        }

        @Override
        public Double getScore() {
            return score;
        }
    }
}
