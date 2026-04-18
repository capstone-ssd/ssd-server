package or.hyu.ssd.api.config;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AutoConfigurationPackage(basePackages = "or.hyu.ssd.infra.persistence")
@EnableJpaRepositories(basePackages = "or.hyu.ssd.infra.persistence")
public class JpaBootstrapConfig {
}
