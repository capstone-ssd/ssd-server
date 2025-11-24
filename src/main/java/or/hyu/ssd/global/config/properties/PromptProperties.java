package or.hyu.ssd.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.prompts")
public class PromptProperties {

    private ChecklistPrompt checklist = new ChecklistPrompt();

    @Getter
    @Setter
    public static class ChecklistPrompt {
        private String system;
        private String user;
    }
}

