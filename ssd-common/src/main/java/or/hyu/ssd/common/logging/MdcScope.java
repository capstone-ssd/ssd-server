package or.hyu.ssd.common.logging;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MdcScope implements AutoCloseable {

    private final Map<String, String> previousValues = new LinkedHashMap<>();

    private MdcScope(Map<String, String> values) {
        values.forEach((key, value) -> {
            previousValues.put(key, MDC.get(key));
            if (value == null || value.isBlank()) {
                MDC.remove(key);
                return;
            }
            MDC.put(key, value);
        });
    }

    public static MdcScope with(String key, String value) {
        return new MdcScope(Map.of(key, value));
    }

    public static MdcScope with(Map<String, String> values) {
        return new MdcScope(values);
    }

    @Override
    public void close() {
        previousValues.forEach((key, value) -> {
            if (value == null) {
                MDC.remove(key);
                return;
            }
            MDC.put(key, value);
        });
    }
}
