package or.hyu.ssd.infra.storage.config;

import or.hyu.ssd.global.config.properties.S3Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3StorageConfig {

    @Bean
    public S3Client s3Client(S3Properties s3Properties) {
        if (!StringUtils.hasText(s3Properties.getRegion())) {
            throw new IllegalStateException("app.storage.s3.region 값이 필요합니다");
        }
        if (!StringUtils.hasText(s3Properties.getBucket())) {
            throw new IllegalStateException("app.storage.s3.bucket 값이 필요합니다");
        }

        var builder = S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder().build());

        if (StringUtils.hasText(s3Properties.getAccessKey()) && StringUtils.hasText(s3Properties.getSecretKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
