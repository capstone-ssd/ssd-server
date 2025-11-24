package or.hyu.ssd.domain.ai.util;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiTextClient {

    private final ChatClient.Builder chatClientBuilder;

    public String complete(String prompt) {
        return chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
