package or.hyu.ssd.api.document.request;

final class RequestStringSanitizer {

    private RequestStringSanitizer() {
    }

    static String stripNullChar(String value) {
        if (value == null || !value.contains("\u0000")) {
            return value;
        }
        return value.replace("\u0000", "");
    }
}
