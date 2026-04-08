package or.hyu.ssd.global.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.domain.member.service.CustomUserDetailsService;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.jwt.repository.AccessTokenBlacklistRepository;
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
        if (!authorizationHeader.startsWith("Bearer ")) {
            setErrorResponse(response, ErrorCode.ACCESS_INVALID_TYPE);
            return;
        }
        String accessToken = BearerTokenExtractor.extract(authorizationHeader);

        try {
            jwtUtil.isExpired(accessToken);
            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                setErrorResponse(response, ErrorCode.ACCESS_INVALID_TYPE);
                return;
            }
            String jti = jwtUtil.getJti(accessToken);
            if (accessTokenBlacklistRepository.exists(jti)) {
                setErrorResponse(response, ErrorCode.ACCESS_TOKEN_BLACKLISTED);
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
        } catch (ExpiredJwtException e) {
            setErrorResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            setErrorResponse(response, ErrorCode.ROLE_INVALID_TYPE);
        } catch (UserExceptionHandler e) {
            if (e.getErrorCode() == ErrorCode.MEMBER_NOT_FOUND) {
                setErrorResponse(response, ErrorCode.TOKEN_MEMBER_NOT_FOUND);
                return;
            }
            throw e;
        } catch (JwtException e) {
            setErrorResponse(response, resolveJwtErrorCode(e));
        }
    }


    /**
     * 필터단에서 발생하는 예외는 저희가 만든 예외 핸들러로는 캐치 할 수 없습니다.
     * 서블렛까지 들어가기 전에 예외가 발생하면 return 시켜버리기 때문이죠. 그래서 이렇게 리스폰스에 직접 데이터를 넣어서 반환합니다
     * */
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"code\":\"%s\", \"msg\":\"%s\"}",
                errorCode.getCode(), errorCode.getMessage()
        ));
    }

    private ErrorCode resolveJwtErrorCode(JwtException e) {
        String simpleName = e.getClass().getSimpleName();
        if ("SignatureException".equals(simpleName) || "MalformedJwtException".equals(simpleName)) {
            return ErrorCode.INVALID_SIGNATURE;
        }
        return ErrorCode.INVALID_TOKEN;
    }

}
