package or.hyu.ssd.domain.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String sayHello() {
        return chatClient
                .prompt()
                .user("안녕")
                .call()
                .content();
    }
}

