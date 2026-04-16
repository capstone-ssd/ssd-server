package or.hyu.ssd.common.property;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhiteListConfigTest {

    @Test
    @DisplayName("actuatorWhitelist()는 프로메테우스와 헬스 엔드포인트를 포함한다")
    void actuatorWhitelist_containsMonitoringEndpoints() {
        // given
        
        // when
        java.util.List<String> whitelist = WhiteListConfig.actuatorWhitelist();

        // then
        assertThat(whitelist)
                .contains("/actuator/prometheus", "/actuator/health", "/actuator/health/**");
    }

    @Test
    @DisplayName("oauthWhitelist()는 메인 카카오 로그인 플로우만 허용한다")
    void oauthWhitelist_containsOnlyMainKakaoLoginFlow() {
        // given
        
        // when
        java.util.List<String> whitelist = WhiteListConfig.oauthWhitelist();

        // then
        assertThat(whitelist)
                .contains("/oauth/kakao/login", "/oauth/kakao/callback")
                .doesNotContain("/oauth/kakao", "/oauth/kakao/server", "/oauth/kakao/server/callback");
    }
}
