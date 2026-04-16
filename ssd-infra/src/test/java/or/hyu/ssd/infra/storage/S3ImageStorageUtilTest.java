package or.hyu.ssd.infra.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.common.exception.StorageException;
import or.hyu.ssd.common.property.S3Properties;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ImageStorageUtilTest {

    @Mock
    private S3Client s3Client;

    private S3ImageStorageUtil s3ImageStorageUtil;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("ssd-images");
        s3Properties.setRegion("ap-northeast-2");
        s3ImageStorageUtil = new S3ImageStorageUtil(s3Client, s3Properties);
    }

    @Test
    void uploadReturnsResolvedUrl() {
        // given
        byte[] payload = "image-bytes".getBytes();

        // when
        String url = s3ImageStorageUtil.upload("documents/test.png", payload, "image/png");

        // then
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("ssd-images");
        assertThat(captor.getValue().key()).isEqualTo("documents/test.png");
        assertThat(captor.getValue().contentType()).isEqualTo("image/png");
        assertThat(url).isEqualTo("https://ssd-images.s3.ap-northeast-2.amazonaws.com/documents/test.png");
    }

    @Test
    void downloadReturnsBytes() {
        // given
        byte[] payload = "downloaded".getBytes();
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), payload));

        // when
        byte[] result = s3ImageStorageUtil.download("documents/test.png");

        // then
        assertThat(result).isEqualTo(payload);
    }

    @Test
    void deleteCallsS3() {
        // given
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());

        // when
        s3ImageStorageUtil.delete("documents/test.png");

        // then
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void existsReturnsTrueWhenHeadSucceeds() {
        // given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        // when
        boolean exists = s3ImageStorageUtil.exists("documents/test.png");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsReturnsFalseWhenKeyMissing() {
        // given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // when
        boolean exists = s3ImageStorageUtil.exists("documents/test.png");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void uploadThrowsStorageExceptionWhenKeyIsBlank() {
        // given
        // when
        // then
        assertThatThrownBy(() -> s3ImageStorageUtil.upload(" ", "a".getBytes(), "image/png"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("스토리지 key는 비어 있을 수 없습니다");
    }

    @Test
    void downloadThrowsStorageExceptionWhenS3Fails() {
        // given
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(new RuntimeException("boom"));

        // when
        // then
        assertThatThrownBy(() -> s3ImageStorageUtil.download("documents/test.png"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("S3 조회에 실패했습니다");
    }
}
