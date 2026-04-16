package or.hyu.ssd.common.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sentry")
public class SentryProperties {
    private String dsn;
    private String environment;
    private boolean sendDefaultPii = true;

    public boolean hasDsn() {
        return dsn != null && !dsn.isBlank();
    }
}
