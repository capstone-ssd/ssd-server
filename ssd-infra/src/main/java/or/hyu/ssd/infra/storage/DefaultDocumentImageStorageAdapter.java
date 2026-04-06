package or.hyu.ssd.infra.storage;

import or.hyu.ssd.domain.document.port.DocumentImageStoragePort;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.StorageException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(DocumentImageStoragePort.class)
public class DefaultDocumentImageStorageAdapter implements DocumentImageStoragePort {

    @Override
    public String upload(String key, byte[] bytes, String contentType) {
        throw new StorageException(ErrorCode.STORAGE_NOT_CONFIGURED, "S3 스토리지 설정이 없어 이미지를 저장할 수 없습니다");
    }
}
