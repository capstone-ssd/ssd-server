package or.hyu.ssd.external.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {
    private String webhookUrl;
    private String grafanaBaseUrl;
    private String loggingDashboardUid = "ssd-logging-overview";

    public boolean hasWebhookUrl() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    public boolean hasGrafanaBaseUrl() {
        return grafanaBaseUrl != null && !grafanaBaseUrl.isBlank();
    }
}
