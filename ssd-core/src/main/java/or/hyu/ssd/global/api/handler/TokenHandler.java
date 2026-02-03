package or.hyu.ssd.global.api.handler;

import or.hyu.ssd.global.api.ErrorCode;

public class TokenHandler extends CustomException{
    public TokenHandler(ErrorCode errorCode) {
        super(errorCode);
    }
}
