package or.hyu.ssd.global.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.external-ai")
public class ExternalAiProperties {
    private String baseUrl;

    @PostConstruct
    public void validate() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("EXTERNAL_AI_BASE_URL 환경변수는 필수입니다.");
        }

        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (Exception e) {
            throw new IllegalStateException("EXTERNAL_AI_BASE_URL 형식이 올바르지 않습니다.");
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException("EXTERNAL_AI_BASE_URL은 반드시 HTTPS 스킴이어야 합니다.");
        }
    }
}
