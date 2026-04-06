package or.hyu.ssd.domain.document.port;

public interface DocumentImageStoragePort {

    String upload(String key, byte[] bytes, String contentType);
}
