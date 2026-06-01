package or.hyu.ssd.external.alert.discord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import or.hyu.ssd.external.config.DiscordProperties;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordWebhookNotifierTest {

    @Test
    @DisplayName("디스코드 본문이 2000자를 넘으면 잘라낸다")
    void truncateMessageWhenContentTooLong() throws Exception {
        // given
        DiscordWebhookNotifier notifier = new DiscordWebhookNotifier(new DiscordProperties(), new MockEnvironment());
        Method truncateMethod = DiscordWebhookNotifier.class.getDeclaredMethod("truncateForDiscord", String.class);
        truncateMethod.setAccessible(true);
        String longMessage = "a".repeat(2100);

        // when
        String truncated = (String) truncateMethod.invoke(notifier, longMessage);

        // then
        assertThat(truncated.length()).isLessThanOrEqualTo(2000);
        assertThat(truncated).endsWith("\n...(truncated)");
    }

    @Test
    @DisplayName("디스코드 본문이 2000자 이하면 그대로 둔다")
    void keepMessageWhenWithinLimit() throws Exception {
        // given
        DiscordWebhookNotifier notifier = new DiscordWebhookNotifier(new DiscordProperties(), new MockEnvironment());
        Method truncateMethod = DiscordWebhookNotifier.class.getDeclaredMethod("truncateForDiscord", String.class);
        truncateMethod.setAccessible(true);
        String message = "a".repeat(2000);

        // when
        String truncated = (String) truncateMethod.invoke(notifier, message);

        // then
        assertThat(truncated).isEqualTo(message);
    }
}
