package or.hyu.ssd.common.logging;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
        Objects.requireNonNull(key, "key must not be null");
        Map<String, String> values = new LinkedHashMap<>();
        values.put(key, value);
        return new MdcScope(values);
    }

    public static MdcScope with(Map<String, String> values) {
        Objects.requireNonNull(values, "values must not be null");
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
