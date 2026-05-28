package or.hyu.ssd.auth.jwt.filter;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.auth.config.JWTConfig;
import or.hyu.ssd.auth.jwt.repository.AccessTokenBlacklistRepository;
import or.hyu.ssd.auth.jwt.support.BearerTokenExtractor;
import or.hyu.ssd.auth.jwt.support.JWTUtil;
import or.hyu.ssd.member.domain.model.Role;
import or.hyu.ssd.auth.principal.CustomUserDetails;
import or.hyu.ssd.auth.principal.CustomUserDetailsService;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.TokenHandler;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import or.hyu.ssd.common.api.ApiErrorResponseWriter;
import or.hyu.ssd.common.logging.LoggingMdcKey;
import or.hyu.ssd.common.logging.MdcScope;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


/**
 * 토큰을 검증하는 클래스 입니다
 * 토큰의 검증은 컨트롤러단이 아닌, 필터 단에서 구현되어 서블릿에 접근하기 전에 통일된 검증을 진행합니다
 *
 * 이때 OncePerRequestFilter 를 상속받아,
 * 요청의 수명주기 안에서 단 한 번만 실행되어 필요없는 검증이 추가적으로 실행되는 오버헤드를 방지합니다
 * */
@RequiredArgsConstructor
@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter{

    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final CustomUserDetailsService customUserDetailsService;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(jwtConfig.getHeader());

        // Authorization 헤더가 없으면 Security EntryPoint에서 401을 반환합니다.
        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String accessToken = BearerTokenExtractor.extract(authorizationHeader);
            jwtUtil.isExpired(accessToken);
            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                logAuthFailure(ErrorCode.ACCESS_INVALID_TYPE, "액세스 토큰 타입이 올바르지 않습니다.");
                ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_INVALID_TYPE);
                return;
            }
            String jti = jwtUtil.getJti(accessToken);
            if (accessTokenBlacklistRepository.isBlacklisted(jti)) {
                logAuthFailure(ErrorCode.ACCESS_TOKEN_BLACKLISTED, "블랙리스트에 등록된 액세스 토큰입니다.");
                ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_TOKEN_BLACKLISTED);
                return;
            }
            Long id = jwtUtil.getId(accessToken);
            String roleString = jwtUtil.getRole(accessToken);
            Role.valueOf(roleString);
            CustomUserDetails customUserDetails = customUserDetailsService.loadUserById(id);

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            MDC.put(LoggingMdcKey.MEMBER_ID, String.valueOf(id));
            filterChain.doFilter(request, response);
        } catch (TokenHandler e) {
            logAuthFailure(e.getErrorCode(), "토큰 처리 중 인증 실패가 발생했습니다.");
            ApiErrorResponseWriter.write(response, e.getErrorCode());
        } catch (ExpiredJwtException e) {
            logAuthFailure(ErrorCode.ACCESS_TOKEN_EXPIRED, "액세스 토큰이 만료되었습니다.");
            ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            logAuthFailure(ErrorCode.ROLE_INVALID_TYPE, "토큰 역할 값이 올바르지 않습니다.");
            ApiErrorResponseWriter.write(response, ErrorCode.ROLE_INVALID_TYPE);
        } catch (UserExceptionHandler e) {
            if (e.getErrorCode() == ErrorCode.MEMBER_NOT_FOUND) {
                logAuthFailure(ErrorCode.TOKEN_MEMBER_NOT_FOUND, "토큰의 회원을 찾을 수 없습니다.");
                ApiErrorResponseWriter.write(response, ErrorCode.TOKEN_MEMBER_NOT_FOUND);
                return;
            }
            throw e;
        } catch (JwtException e) {
            ErrorCode errorCode = resolveJwtErrorCode(e);
            logAuthFailure(errorCode, "JWT 검증에 실패했습니다.");
            ApiErrorResponseWriter.write(response, errorCode);
        }
    }

    private ErrorCode resolveJwtErrorCode(JwtException e) {
        String simpleName = e.getClass().getSimpleName();
        if ("SignatureException".equals(simpleName) || "MalformedJwtException".equals(simpleName)) {
            return ErrorCode.INVALID_SIGNATURE;
        }
        return ErrorCode.INVALID_TOKEN;
    }

    private void logAuthFailure(ErrorCode errorCode, String message) {
        try (MdcScope ignored = MdcScope.with(Map.of(
                LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_AUTH,
                LoggingMdcKey.REASON, errorCode.getCode(),
                LoggingMdcKey.RESPONSE_STATUS, String.valueOf(errorCode.getStatus().value())
        ))) {
            log.warn("인증 실패: {}", message);
        }
    }

}
