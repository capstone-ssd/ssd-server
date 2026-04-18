package or.hyu.ssd.api.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "or.hyu.ssd")
@EnableFeignClients(basePackages = "or.hyu.ssd")
public class SsdApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsdApplication.class, args);
    }
}
