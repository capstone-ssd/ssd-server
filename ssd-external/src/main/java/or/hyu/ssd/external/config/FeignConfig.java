package or.hyu.ssd.external.config;

import feign.Request;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.external.config.FeignProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final FeignProperties feignProperties;

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                feignProperties.getConnectTimeout(), TimeUnit.MILLISECONDS,
                feignProperties.getReadTimeout(), TimeUnit.MILLISECONDS,
                true
        );
    }
}
