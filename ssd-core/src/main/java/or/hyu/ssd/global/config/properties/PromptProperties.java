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
    private SummaryPrompt summary = new SummaryPrompt();
    private EvaluationPrompt evaluation = new EvaluationPrompt();
    private DetailsPrompt details = new DetailsPrompt();
    private EvaluatorChecklistPrompt evaluatorChecklist = new EvaluatorChecklistPrompt();

    @Getter
    @Setter
    public static class ChecklistPrompt {
        private String system;
        private String user;
    }

    @Getter
    @Setter
    public static class SummaryPrompt {
        private String system;
        private String user;
    }

    @Getter
    @Setter
    public static class EvaluationPrompt {
        private String system;
        private String user;
    }

    @Getter
    @Setter
    public static class DetailsPrompt {
        private String system;
        private String user;
    }

    @Getter
    @Setter
    public static class EvaluatorChecklistPrompt {
        private String system;
        private String user;
    }
}
