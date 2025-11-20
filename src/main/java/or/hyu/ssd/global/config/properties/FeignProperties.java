package or.hyu.ssd.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "feign.client.config.default")
public class FeignProperties {
    private int connectTimeout;
    private int readTimeout;
}