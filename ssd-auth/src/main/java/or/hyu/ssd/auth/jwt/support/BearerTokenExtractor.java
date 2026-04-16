package or.hyu.ssd.auth.jwt.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.TokenHandler;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BearerTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public static String extract(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new TokenHandler(ErrorCode.ACCESS_TOKEN_REQUIRED);
        }
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new TokenHandler(ErrorCode.ACCESS_INVALID_TYPE);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
