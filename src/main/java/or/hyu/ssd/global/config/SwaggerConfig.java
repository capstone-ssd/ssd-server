package or.hyu.ssd.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SSD API")
                        .description("SSD API 명세서 입니다")
                        .version("v1")
                        .contact(new Contact().name("SSD Team"))
                        .license(new License().name("Apache 2.0")))
                .addServersItem(new Server().url("/").description("Default"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("or.hyu.ssd")
                .pathsToMatch("/**")
                .build();
    }
}

