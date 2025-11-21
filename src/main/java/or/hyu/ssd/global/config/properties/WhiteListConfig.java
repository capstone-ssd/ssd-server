package or.hyu.ssd.global.config.properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WhiteListConfig {

    // 스웨거 관련 인가 설정
    public static final List<String> swaggerWhitelist() {
        return List.of(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        );
    }

    // oauth 관련 인가 설정
    public static final List<String> oauthWhitelist() {
        return List.of(
                "/oauth/kakao",
                "/oauth/kakao/",
                "/oauth/kakao/callback",
                "/oauth/kakao/server",
                "/oauth/kakao/server/callback",
                "/access",
                "/reissue"
        );
    }
}
