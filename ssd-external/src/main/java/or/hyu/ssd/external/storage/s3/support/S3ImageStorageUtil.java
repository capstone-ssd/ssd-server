package or.hyu.ssd.external.storage.s3.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.StorageException;
import or.hyu.ssd.common.property.S3Properties;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3ImageStorageUtil {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String upload(String key, byte[] bytes, String contentType) {
        validateKey(key);
        if (bytes == null || bytes.length == 0) {
            throw new StorageException(ErrorCode.STORAGE_UPLOAD_FAILED, "업로드할 파일 데이터가 비어 있습니다");
        }

        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key);

            if (contentType != null && !contentType.isBlank()) {
                requestBuilder.contentType(contentType);
            }

            s3Client.putObject(requestBuilder.build(), RequestBody.fromBytes(bytes));
            return resolveUrl(key);
        } catch (Exception e) {
            throw new StorageException(ErrorCode.STORAGE_UPLOAD_FAILED, "S3 업로드에 실패했습니다: " + key);
        }
    }

    public byte[] download(String key) {
        validateKey(key);
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(key)
                            .build()
            );
            return response.asByteArray();
        } catch (Exception e) {
            throw new StorageException(ErrorCode.STORAGE_DOWNLOAD_FAILED, "S3 조회에 실패했습니다: " + key);
        }
    }

    public void delete(String key) {
        validateKey(key);
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(key)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException(ErrorCode.STORAGE_DELETE_FAILED, "S3 삭제에 실패했습니다: " + key);
        }
    }

    public boolean exists(String key) {
        validateKey(key);
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(key)
                            .build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new StorageException(ErrorCode.STORAGE_DOWNLOAD_FAILED, "S3 존재 여부 확인에 실패했습니다: " + key);
        } catch (Exception e) {
            throw new StorageException(ErrorCode.STORAGE_DOWNLOAD_FAILED, "S3 존재 여부 확인에 실패했습니다: " + key);
        }
    }

    public String resolveUrl(String key) {
        validateKey(key);
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(
                s3Properties.getBucket(),
                s3Properties.getRegion(),
                key
        );
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new StorageException(ErrorCode.STORAGE_UPLOAD_FAILED, "스토리지 key는 비어 있을 수 없습니다");
        }
    }
}
