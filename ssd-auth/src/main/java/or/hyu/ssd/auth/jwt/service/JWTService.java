package or.hyu.ssd.auth.jwt.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.auth.jwt.support.RefreshTokenValidator;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import or.hyu.ssd.auth.jwt.property.CookieConfig;
import or.hyu.ssd.auth.jwt.property.JWTConfig;
import or.hyu.ssd.auth.jwt.support.JWTUtil;
import or.hyu.ssd.auth.jwt.repository.AccessTokenBlacklistRepository;
import or.hyu.ssd.auth.jwt.repository.RefreshTokenRepository;
import or.hyu.ssd.auth.jwt.support.CookieUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JWTService {
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private final CookieConfig cookieConfig;
    private final RefreshTokenValidator refreshTokenValidator;


    // 토큰 발급기를 위한 메서드입니다
    public void issueTestAccessToken(HttpServletResponse response) {

        String access = jwtUtil.createJwt("access", 1L, "ROLE_AUTHOR", jwtConfig.getAccessTokenValidityInSeconds());
        response.setHeader("access-token", access);
    }


    /**
     * 액세스 토큰이 만료가 되면 발생하는 예외를 클라이언트가 받게되면
     * 서버에 RTT 로직을 호출합니다.
     *
     * 그러면 서버에서는 해당 로직을 수행합니다
     *
     * 만약 여기서 걸리는 경우가 존재한다면, 새롭게 로그인을 하여 리프레시 토큰을 발급받아야 합니다.
     * */
    public void reissueTokens(HttpServletRequest request, HttpServletResponse response){
        Long memberId = refreshTokenValidator.extractValidMemberId(request);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = jwtUtil.createJwt("access", member.getId(), member.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refreshToken = jwtUtil.createJwt("refresh", member.getId(), member.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        rotateRefreshToken(memberId, refreshToken);
        writeIssuedTokens(response, accessToken, refreshToken);
    }

    public void logout(Long userId, String accessToken, HttpServletResponse response) {
        long remainingExpiration = jwtUtil.getRemainingExpiration(accessToken);
        if (remainingExpiration > 0) {
            accessTokenBlacklistRepository.blacklist(jwtUtil.getJti(accessToken), remainingExpiration);
        }

        refreshTokenRepository.deleteById(userId);

        CookieUtil.expireSameSiteCookie(
                response,
                "refresh-token",
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );
    }

    private void rotateRefreshToken(Long memberId, String refreshToken) {
        refreshTokenRepository.deleteById(memberId);
        refreshTokenRepository.saveRefreshToken(memberId, refreshToken, jwtConfig.getRefreshTokenValidityInSeconds());
    }

    private void writeIssuedTokens(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader("access-token", accessToken);
        CookieUtil.addSameSiteCookie(
                response,
                "refresh-token",
                refreshToken,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );
    }
}
