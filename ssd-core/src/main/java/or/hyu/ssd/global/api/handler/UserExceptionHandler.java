package or.hyu.ssd.global.api.handler;

import or.hyu.ssd.global.api.ErrorCode;

public class UserExceptionHandler extends CustomException {
    public UserExceptionHandler(ErrorCode errorCode) {
        super(errorCode);
    }
}
