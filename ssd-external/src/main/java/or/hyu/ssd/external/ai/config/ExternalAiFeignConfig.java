package or.hyu.ssd.external.ai.config;

import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class ExternalAiFeignConfig {

    @Bean
    public Request.Options externalAiRequestOptions() {
        log.info("[ExternalAiClient Timeout] timeout disabled");
        return new Request.Options(Duration.ZERO, Duration.ZERO, true);
    }
}
