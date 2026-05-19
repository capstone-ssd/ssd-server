package or.hyu.ssd.document.application.result;

public record DocumentSearchSuggestionResult(
        String keyword,
        double score
) {
    public static DocumentSearchSuggestionResult of(String keyword, double score) {
        return new DocumentSearchSuggestionResult(keyword, score);
    }
}
