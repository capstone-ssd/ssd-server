package or.hyu.ssd.common.exception;

import or.hyu.ssd.common.exception.ErrorCode;

public class TokenHandler extends CustomException{
    public TokenHandler(ErrorCode errorCode) {
        super(errorCode);
    }
}
