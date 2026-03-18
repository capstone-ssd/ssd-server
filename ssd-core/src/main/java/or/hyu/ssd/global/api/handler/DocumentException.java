package or.hyu.ssd.global.api.handler;

import or.hyu.ssd.global.api.ErrorCode;

public class DocumentException extends CustomException {
    public DocumentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DocumentException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
