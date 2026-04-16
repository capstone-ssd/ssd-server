package or.hyu.ssd.common.exception;

import or.hyu.ssd.common.exception.ErrorCode;

public class StorageException extends CustomException {
    public StorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public StorageException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
