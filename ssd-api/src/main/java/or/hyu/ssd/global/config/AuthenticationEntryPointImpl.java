package or.hyu.ssd.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import or.hyu.ssd.global.api.ApiResponse;
import or.hyu.ssd.global.api.ErrorCode;
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
        writeErrorResponse(response, ErrorCode.ACCESS_TOKEN_REQUIRED);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.fail(errorCode);
        response.getWriter().write(String.format(
                "{\"code\":\"%s\",\"msg\":\"%s\"}",
                apiResponse.code(),
                apiResponse.msg()
        ));
    }
}
