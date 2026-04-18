package or.hyu.ssd.infra.adapter.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.port.DocumentImageStoragePort;
import or.hyu.ssd.external.storage.s3.support.S3ImageStorageUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3DocumentImageStorageAdapter implements DocumentImageStoragePort {

    private final S3ImageStorageUtil s3ImageStorageUtil;

    @Override
    public String upload(String key, byte[] bytes, String contentType) {
        return s3ImageStorageUtil.upload(key, bytes, contentType);
    }
}
