package or.hyu.ssd.auth.oauth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuthRedirectStateRepository {

    private static final String PREFIX = "oauth-redirect-state:";

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String state, String redirectUri, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(state), redirectUri, Duration.ofSeconds(ttlSeconds));
    }

    public Optional<String> consume(String state) {
        String key = key(state);
        String redirectUri = redisTemplate.opsForValue().get(key);
        if (redirectUri == null) {
            return Optional.empty();
        }
        redisTemplate.delete(key);
        return Optional.of(redirectUri);
    }

    private String key(String state) {
        return PREFIX + state;
    }
}
