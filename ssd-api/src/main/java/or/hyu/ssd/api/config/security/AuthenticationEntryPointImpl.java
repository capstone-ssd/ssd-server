package or.hyu.ssd.api.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.api.ApiErrorResponseWriter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        ApiErrorResponseWriter.write(response, ErrorCode.ACCESS_TOKEN_REQUIRED);
    }
}
