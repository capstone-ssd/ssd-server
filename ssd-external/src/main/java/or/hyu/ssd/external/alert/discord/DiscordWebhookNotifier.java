package or.hyu.ssd.external.alert.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.common.alert.ErrorAlertContext;
import or.hyu.ssd.common.alert.ErrorAlertNotifier;
import or.hyu.ssd.external.config.DiscordProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookNotifier implements ErrorAlertNotifier {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DISCORD_CONTENT_LIMIT = 2000;
    private static final String TRUNCATION_SUFFIX = "\n...(truncated)";

    private final DiscordProperties discordProperties;
    private final Environment environment;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();

    @Override
    public void notify(ErrorAlertContext context) {
        send(buildErrorMessage(context));
    }

    private void send(String message) {
        if (!discordProperties.hasWebhookUrl()) {
            log.debug("Discord webhook URL이 설정되지 않아 전송을 건너뜁니다.");
            return;
        }

        try {
            String payload = buildPayload(message);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(discordProperties.getWebhookUrl()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Discord webhook 전송에 실패했습니다. status={}, body={}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Discord webhook 전송 중 예외가 발생했습니다.", e);
        }
    }

    private String buildErrorMessage(ErrorAlertContext context) {
        return """
                [SSD 서버 예외 알림]
                - 시간: %s
                - 환경: %s
                - Request ID: %s
                - Grafana 로그: %s
                - 코드: %s
                - 상태: %d
                - 메서드: %s
                - 경로: %s
                - IP: %s
                - 예외: %s
                - 메시지: %s
                """.formatted(
                LocalDateTime.now().format(TIME_FORMATTER),
                resolveEnvironment(),
                context.requestId(),
                buildGrafanaLogUrl(context.requestId()),
                context.errorCode(),
                context.status(),
                context.method(),
                context.uri(),
                context.clientIp(),
                context.exceptionClass(),
                context.message()
        );
    }

    private String buildGrafanaLogUrl(String requestId) {
        if (!discordProperties.hasGrafanaBaseUrl() || requestId == null || requestId.isBlank() || "(omitted)".equals(requestId)) {
            return "(not configured)";
        }

        String baseUrl = trimTrailingSlash(discordProperties.getGrafanaBaseUrl());
        String dashboardUid = discordProperties.getLoggingDashboardUid();
        String encodedRequestId = URLEncoder.encode(requestId, StandardCharsets.UTF_8);
        return "%s/d/%s?orgId=1&var-requestId=%s".formatted(baseUrl, dashboardUid, encodedRequestId);
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String resolveEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(",", Arrays.asList(activeProfiles));
    }

    private String buildPayload(String content) {
        return "{\"content\":\"" + escapeJson(truncateForDiscord(content)) + "\"}";
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String truncateForDiscord(String content) {
        if (content == null || content.length() <= DISCORD_CONTENT_LIMIT) {
            return content;
        }

        int maxPrefixLength = DISCORD_CONTENT_LIMIT - TRUNCATION_SUFFIX.length();
        if (maxPrefixLength <= 0) {
            return content.substring(0, DISCORD_CONTENT_LIMIT);
        }
        return content.substring(0, maxPrefixLength) + TRUNCATION_SUFFIX;
    }
}
