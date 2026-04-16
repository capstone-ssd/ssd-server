package or.hyu.ssd.common.exception;

import or.hyu.ssd.common.exception.ErrorCode;

public class DocumentException extends CustomException {
    public DocumentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DocumentException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
