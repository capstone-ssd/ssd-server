package or.hyu.ssd.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String msg, T data) {

    public static <T> ApiResponse<T> ok(T data, String msg) {
        return new ApiResponse<T>(String.valueOf(HttpStatus.OK.value()), msg, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(String.valueOf(HttpStatus.OK.value()), "요청이 성공적으로 처리 되었습니다", data);
    }


    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
