package or.hyu.ssd.infra.storage;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.port.DocumentImageStoragePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(S3ImageStorageUtil.class)
public class S3DocumentImageStorageAdapter implements DocumentImageStoragePort {

    private final S3ImageStorageUtil s3ImageStorageUtil;

    @Override
    public String upload(String key, byte[] bytes, String contentType) {
        return s3ImageStorageUtil.upload(key, bytes, contentType);
    }
}
