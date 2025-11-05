package or.hyu.ssd.global.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"SERVER5001","서버에서 예외가 발생하였습니다. 개발자에게 문의해주세요");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

