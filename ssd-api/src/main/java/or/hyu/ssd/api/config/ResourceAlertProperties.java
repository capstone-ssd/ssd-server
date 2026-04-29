package or.hyu.ssd.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "monitoring.resource-alert")
public class ResourceAlertProperties {

    private boolean enabled = false;
    private long checkIntervalMs = 60_000L;
    private Duration cooldown = Duration.ofMinutes(5);
    private int consecutiveThreshold = 3;
    private String diskPath = "/";
    private double diskUsageThreshold = 0.85;
    private double cpuUsageThreshold = 0.80;
    private double heapUsageThreshold = 0.80;
    private double gcPauseAverageThresholdMs = 300.0;
    private double hikariPendingThreshold = 1.0;
    private boolean externalAiHealthEnabled = true;
}
