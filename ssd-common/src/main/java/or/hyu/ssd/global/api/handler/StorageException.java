package or.hyu.ssd.global.api.handler;

import or.hyu.ssd.global.api.ErrorCode;

public class StorageException extends CustomException {
    public StorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public StorageException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
