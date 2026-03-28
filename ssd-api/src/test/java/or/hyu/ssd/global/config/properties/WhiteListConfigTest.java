package or.hyu.ssd.global.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhiteListConfigTest {

    @Test
    @DisplayName("actuatorWhitelist()는 프로메테우스와 헬스 엔드포인트를 포함한다")
    void actuatorWhitelist_containsMonitoringEndpoints() {
        assertThat(WhiteListConfig.actuatorWhitelist())
                .contains("/actuator/prometheus", "/actuator/health", "/actuator/health/**");
    }

    @Test
    @DisplayName("oauthWhitelist()는 카카오 로그인 redirect 시작 엔드포인트를 포함한다")
    void oauthWhitelist_containsKakaoRedirectLoginEndpoint() {
        assertThat(WhiteListConfig.oauthWhitelist())
                .contains("/oauth/kakao/login");
    }
}
