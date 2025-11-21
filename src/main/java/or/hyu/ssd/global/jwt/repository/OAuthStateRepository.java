package or.hyu.ssd.global.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OAuth state 값을 1회성으로 저장/소비하는 저장소입니다.
 * - CSRF/재사용 방지용 state -> redirect_uri 매핑을 보관합니다.
 * - TTL을 짧게(예: 5분) 유지합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuthStateRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "oauth-state:";

    public void store(String state, String redirectUri, long ttlSeconds) {
        String key = PREFIX + state;
        redisTemplate.opsForValue().set(key, redirectUri, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 저장된 state를 조회 후 즉시 삭제하여 1회성으로 소비합니다.
     */
    public String consume(String state) {
        String key = PREFIX + state;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            redisTemplate.delete(key);
        }
        return value;
    }
}

