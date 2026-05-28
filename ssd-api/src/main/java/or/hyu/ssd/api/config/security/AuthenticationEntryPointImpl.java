package or.hyu.ssd.api.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.api.ApiErrorResponseWriter;
import or.hyu.ssd.common.logging.LoggingMdcKey;
import or.hyu.ssd.common.logging.MdcScope;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        try (MdcScope ignored = MdcScope.with(Map.of(
                LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_AUTH,
                LoggingMdcKey.REASON, ErrorCode.ACCESS_TOKEN_REQUIRED.getCode(),
                LoggingMdcKey.RESPONSE_STATUS, String.valueOf(ErrorCode.ACCESS_TOKEN_REQUIRED.getStatus().value())
        ))) {
            log.warn("인증 실패: 액세스 토큰이 필요합니다.");
        }
        ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_TOKEN_REQUIRED);
    }
}
