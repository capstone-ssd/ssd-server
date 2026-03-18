package or.hyu.ssd.global.api.handler;

import or.hyu.ssd.global.api.ErrorCode;

public class DomainException extends CustomException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DomainException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
