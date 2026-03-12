package or.hyu.ssd.global.api.handler;

import lombok.Getter;
import or.hyu.ssd.global.api.ErrorCode;

@Getter
public abstract class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage;

    protected CustomException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    protected CustomException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }
}

