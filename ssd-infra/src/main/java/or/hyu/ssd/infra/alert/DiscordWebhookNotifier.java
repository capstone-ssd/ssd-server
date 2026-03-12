package or.hyu.ssd.infra.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.global.alert.ErrorAlertContext;
import or.hyu.ssd.global.alert.ErrorAlertNotifier;
import or.hyu.ssd.global.config.properties.DiscordProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
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

    private final ObjectMapper objectMapper;
    private final DiscordProperties discordProperties;
    private final Environment environment;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();

    @Override
    public void notify(ErrorAlertContext context) {
        if (!discordProperties.hasWebhookUrl()) {
            log.debug("Discord webhook URL이 설정되지 않아 전송을 건너뜁니다.");
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(new DiscordWebhookPayload(buildMessage(context)));
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

    private String buildMessage(ErrorAlertContext context) {
        return """
                [SSD 서버 예외 알림]
                - 시간: %s
                - 환경: %s
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
                context.errorCode(),
                context.status(),
                context.method(),
                context.uri(),
                context.clientIp(),
                context.exceptionClass(),
                context.message()
        );
    }

    private String resolveEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(",", Arrays.asList(activeProfiles));
    }

    private record DiscordWebhookPayload(String content) {
    }
}
