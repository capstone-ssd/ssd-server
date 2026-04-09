package or.hyu.ssd.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.support.ApiErrorResponseWriter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        ApiErrorResponseWriter.write(response, ErrorCode.REQUEST_ACCESS_DENIED);
    }
}
