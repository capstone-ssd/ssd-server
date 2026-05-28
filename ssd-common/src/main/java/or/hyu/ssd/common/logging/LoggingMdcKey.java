package or.hyu.ssd.common.logging;

public final class LoggingMdcKey {

    public static final String REQUEST_ID = "requestId";
    public static final String MEMBER_ID = "memberId";
    public static final String METHOD = "method";
    public static final String URI = "uri";
    public static final String CLIENT_IP = "clientIp";
    public static final String USER_AGENT = "userAgent";
    public static final String RESPONSE_STATUS = "responseStatus";
    public static final String ELAPSED_MS = "elapsedMs";
    public static final String LOG_TYPE = "logType";
    public static final String ACTION = "action";
    public static final String RESULT = "result";
    public static final String REASON = "reason";
    public static final String DOCUMENT_ID = "documentId";
    public static final String FOLDER_ID = "folderId";
    public static final String AI_ENDPOINT = "aiEndpoint";
    public static final String EXTERNAL_STATUS = "externalStatus";

    public static final String LOG_TYPE_HTTP_REQUEST = "http_request";
    public static final String LOG_TYPE_SERVER_EXCEPTION = "server_exception";
    public static final String LOG_TYPE_CLIENT_EXCEPTION = "client_exception";
    public static final String LOG_TYPE_AUTH = "auth";
    public static final String LOG_TYPE_DOCUMENT_CRUD = "document_crud";
    public static final String LOG_TYPE_EXTERNAL_AI = "external_ai";

    private LoggingMdcKey() {
    }
}
