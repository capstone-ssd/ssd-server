package or.hyu.ssd.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import or.hyu.ssd.common.api.ApiResponse;
import or.hyu.ssd.common.exception.ErrorCode;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(ApiResponse.fail(errorCode)));
    }
}
