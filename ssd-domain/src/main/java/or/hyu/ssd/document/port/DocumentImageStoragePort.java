package or.hyu.ssd.document.port;

public interface DocumentImageStoragePort {

    String upload(String key, byte[] bytes, String contentType);
}
