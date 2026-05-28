package or.hyu.ssd.api.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.common.logging.LoggingMdcKey;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String requestId = resolveRequestId(request);

        try {
            response.setHeader(REQUEST_ID_HEADER, requestId);
            putRequestMdc(request, requestId);
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            MDC.put(LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_HTTP_REQUEST);
            MDC.put(LoggingMdcKey.RESPONSE_STATUS, String.valueOf(response.getStatus()));
            MDC.put(LoggingMdcKey.ELAPSED_MS, String.valueOf(elapsedMs));
            log.info("HTTP 요청 처리가 완료되었습니다");
            MDC.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null && SAFE_REQUEST_ID.matcher(requestId).matches()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private void putRequestMdc(HttpServletRequest request, String requestId) {
        MDC.put(LoggingMdcKey.REQUEST_ID, requestId);
        MDC.put(LoggingMdcKey.METHOD, request.getMethod());
        MDC.put(LoggingMdcKey.URI, request.getRequestURI());
        MDC.put(LoggingMdcKey.CLIENT_IP, resolveClientIp(request));
        MDC.put(LoggingMdcKey.USER_AGENT, safeHeader(request.getHeader("User-Agent")));
        MDC.put(LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_HTTP_REQUEST);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String safeHeader(String value) {
        if (value == null || value.isBlank()) {
            return "(omitted)";
        }
        return value.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
