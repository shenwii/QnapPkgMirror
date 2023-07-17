package cn.qnap.mirror.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class Configure {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endPoint;
    private String downloadLink;
    private String region;
}
