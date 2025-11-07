package or.hyu.ssd.global.api.handler;

import lombok.Getter;
import or.hyu.ssd.global.api.ErrorCode;

@Getter
public abstract class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    protected CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

