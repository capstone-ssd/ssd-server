package or.hyu.ssd.domain.member.service.support;

public record KakaoLoginResult(
        String accessToken,
        String refreshToken,
        boolean isNewUser
) {
}
