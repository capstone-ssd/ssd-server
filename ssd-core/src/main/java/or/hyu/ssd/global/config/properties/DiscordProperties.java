package or.hyu.ssd.global.config.properties;

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

    public boolean hasWebhookUrl() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }
}
