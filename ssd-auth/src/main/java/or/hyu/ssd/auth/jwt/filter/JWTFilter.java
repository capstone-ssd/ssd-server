package or.hyu.ssd.auth.jwt.filter;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.auth.jwt.property.JWTConfig;
import or.hyu.ssd.auth.jwt.repository.AccessTokenBlacklistRepository;
import or.hyu.ssd.auth.jwt.support.BearerTokenExtractor;
import or.hyu.ssd.auth.jwt.support.JWTUtil;
import or.hyu.ssd.member.domain.entity.Role;
import or.hyu.ssd.member.application.service.CustomUserDetails;
import or.hyu.ssd.member.application.service.CustomUserDetailsService;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.TokenHandler;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import or.hyu.ssd.common.api.ApiErrorResponseWriter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * 토큰을 검증하는 클래스 입니다
 * 토큰의 검증은 컨트롤러단이 아닌, 필터 단에서 구현되어 서블릿에 접근하기 전에 통일된 검증을 진행합니다
 *
 * 이때 OncePerRequestFilter 를 상속받아,
 * 요청의 수명주기 안에서 단 한 번만 실행되어 필요없는 검증이 추가적으로 실행되는 오버헤드를 방지합니다
 * */
@RequiredArgsConstructor
@Component
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
                ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_INVALID_TYPE);
                return;
            }
            String jti = jwtUtil.getJti(accessToken);
            if (accessTokenBlacklistRepository.isBlacklisted(jti)) {
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
            filterChain.doFilter(request, response);
        } catch (TokenHandler e) {
            ApiErrorResponseWriter.write(response, e.getErrorCode());
        } catch (ExpiredJwtException e) {
            ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            ApiErrorResponseWriter.write(response, ErrorCode.ROLE_INVALID_TYPE);
        } catch (UserExceptionHandler e) {
            if (e.getErrorCode() == ErrorCode.MEMBER_NOT_FOUND) {
                ApiErrorResponseWriter.write(response, ErrorCode.TOKEN_MEMBER_NOT_FOUND);
                return;
            }
            throw e;
        } catch (JwtException e) {
            ApiErrorResponseWriter.write(response, resolveJwtErrorCode(e));
        }
    }

    private ErrorCode resolveJwtErrorCode(JwtException e) {
        String simpleName = e.getClass().getSimpleName();
        if ("SignatureException".equals(simpleName) || "MalformedJwtException".equals(simpleName)) {
            return ErrorCode.INVALID_SIGNATURE;
        }
        return ErrorCode.INVALID_TOKEN;
    }

}
