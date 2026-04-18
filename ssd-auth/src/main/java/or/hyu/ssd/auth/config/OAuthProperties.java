package or.hyu.ssd.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * app.oauth.* 관련 설정값을 바인딩합니다.
 * - allowed-origins: 동적 redirect_uri 생성 시 허용할 Origin 화이트리스트
 * - redirect-state-ttl-seconds: 로그인 redirect state 보관 시간(초)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth")
public class OAuthProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    private long redirectStateTtlSeconds = 300L;
}

