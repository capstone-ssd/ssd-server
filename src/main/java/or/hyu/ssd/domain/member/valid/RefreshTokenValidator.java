package or.hyu.ssd.domain.member.valid;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.member.repository.RefreshTokenRepository;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.TokenHandler;
import or.hyu.ssd.global.jwt.JWTUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenValidator {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public Long validateRefreshToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new TokenHandler(ErrorCode.COOKIE_NULL);

        String refresh = null;

        for (Cookie cookie : cookies) {
            if ("refresh-token".equals(cookie.getName())) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) throw new TokenHandler(ErrorCode.REFRESH_TOKEN_NULL);

        try {

            jwtUtil.isExpired(refresh);

            if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
                throw new TokenHandler(ErrorCode.REFRESH_TOKEN_NULL);
            }

            Long userId = jwtUtil.getId(refresh);
            if (!refreshTokenRepository.existsById(userId)) {
                throw new TokenHandler(ErrorCode.REFRESH_TOKEN_NULL);
            }

            return userId;

        } catch (ExpiredJwtException e) {
            throw new TokenHandler(ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (SignatureException e) {
            throw new TokenHandler(ErrorCode.INVALID_SIGNATURE);
        } catch (Exception e) {
            throw new TokenHandler(ErrorCode.INVALID_TOKEN);
        }
    }
}
