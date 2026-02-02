package or.hyu.ssd.domain.ai.util;

public class PromptComposer {

    public static String mergeSystemUser(String systemPrompt, String userPrompt) {
        String system = systemPrompt == null ? "" : systemPrompt.trim();
        String user = userPrompt == null ? "" : userPrompt.trim();

        if (system.isEmpty()) return user;
        if (user.isEmpty()) return system;
        return system + "\n\n" + user;
    }
}

