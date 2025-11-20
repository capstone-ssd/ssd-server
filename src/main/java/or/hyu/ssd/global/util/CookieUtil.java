package or.hyu.ssd.global.util;

import io.micrometer.common.lang.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;

@Slf4j
public class CookieUtil {


    public static void addSameSiteCookie(HttpServletResponse response,
                                         String name,
                                         String value,
                                         int maxAgeSeconds,
                                         String domain,
                                         boolean secure,
                                         String sameSite) {

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(secure);

        if (StringUtils.hasText(domain)) {
            builder.domain(domain);
        }

        if (StringUtils.hasText(sameSite)) {
            builder.sameSite(sameSite);
        }

        ResponseCookie cookie = builder.build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
