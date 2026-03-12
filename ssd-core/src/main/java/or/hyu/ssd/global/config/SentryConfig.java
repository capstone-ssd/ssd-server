package or.hyu.ssd.global.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.global.config.properties.SentryProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentryConfig {

    private final SentryProperties sentryProperties;

    @PostConstruct
    void init() {
        if (!sentryProperties.hasDsn()) {
            log.info("Sentry DSN이 없어 Sentry 초기화를 건너뜁니다.");
            return;
        }

        Sentry.init(options -> {
            options.setDsn(sentryProperties.getDsn());
            options.setEnvironment(sentryProperties.getEnvironment());
            options.setSendDefaultPii(sentryProperties.isSendDefaultPii());
            options.setMaxRequestBodySize(SentryOptions.RequestSize.ALWAYS);
        });

        log.info("Sentry SDK 초기화를 완료했습니다. environment={}", sentryProperties.getEnvironment());
    }

    @PreDestroy
    void close() {
        if (sentryProperties.hasDsn()) {
            Sentry.close();
        }
    }
}
