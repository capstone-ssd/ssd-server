package or.hyu.ssd.auth.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AccessTokenBlacklistRepository {

    private static final String PREFIX = "access-blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }

    public void blacklist(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(PREFIX + jti, "blacklisted", Duration.ofSeconds(ttlSeconds));
    }
}
