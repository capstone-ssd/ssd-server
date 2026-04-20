package or.hyu.ssd.document.port.dto;

public record ExternalAiHealthStatus(
        boolean available,
        String message
) {
    public static ExternalAiHealthStatus up(String message) {
        return new ExternalAiHealthStatus(true, normalize(message, "외부 AI 서버가 정상 응답했습니다."));
    }

    public static ExternalAiHealthStatus down(String message) {
        return new ExternalAiHealthStatus(false, normalize(message, "외부 AI 서버에 연결할 수 없습니다."));
    }

    private static String normalize(String message, String defaultMessage) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }
        return message.trim();
    }
}
