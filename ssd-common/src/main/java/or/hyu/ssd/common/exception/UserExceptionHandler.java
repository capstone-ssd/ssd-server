package or.hyu.ssd.common.exception;

import or.hyu.ssd.common.exception.ErrorCode;

public class UserExceptionHandler extends CustomException {
    public UserExceptionHandler(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserExceptionHandler(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
