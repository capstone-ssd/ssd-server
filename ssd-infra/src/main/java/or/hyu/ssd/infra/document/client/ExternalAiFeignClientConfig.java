package or.hyu.ssd.infra.document.client;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class ExternalAiFeignClientConfig {

    @Bean
    public Request.Options externalAiRequestOptions(
            @Value("${feign.client.config.externalAiClient.connectTimeout:5000}") int connectTimeout,
            @Value("${feign.client.config.externalAiClient.readTimeout:180000}") int readTimeout
    ) {
        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                true
        );
    }
}
