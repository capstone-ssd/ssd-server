package or.hyu.ssd.auth.oauth.kakao.support;

public record KakaoLoginResult(
        String accessToken,
        String refreshToken,
        boolean isNewUser
) {
}
