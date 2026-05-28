package or.hyu.ssd.api.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.api.ApiErrorResponseWriter;
import or.hyu.ssd.common.logging.LoggingMdcKey;
import or.hyu.ssd.common.logging.MdcScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        try (MdcScope ignored = MdcScope.with(Map.of(
                LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_AUTH,
                LoggingMdcKey.REASON, ErrorCode.REQUEST_ACCESS_DENIED.getCode(),
                LoggingMdcKey.RESPONSE_STATUS, String.valueOf(ErrorCode.REQUEST_ACCESS_DENIED.getStatus().value())
        ))) {
            log.warn("인가 실패: 접근 권한이 없습니다.");
        }
        ApiErrorResponseWriter.write(response, ErrorCode.REQUEST_ACCESS_DENIED);
    }
}
