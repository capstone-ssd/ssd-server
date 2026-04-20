package or.hyu.ssd.document.application.result;

public record ExternalAiHealthResult(
        String status,
        boolean available,
        String message
) {
    public static ExternalAiHealthResult of(boolean available, String message) {
        return new ExternalAiHealthResult(
                available ? "UP" : "DOWN",
                available,
                normalize(message, available ? "외부 AI 서버가 정상 응답했습니다." : "외부 AI 서버에 연결할 수 없습니다.")
        );
    }

    private static String normalize(String message, String defaultMessage) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }
        return message.trim();
    }
}
