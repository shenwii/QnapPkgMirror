package cn.qnap.mirror.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configure类用于读取配置文件中的存储相关属性。
 */
@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class Configure {
    private String accessKey; // 存储访问密钥
    private String secretKey; // 存储访问密钥对应的秘钥
    private String bucket; // 存储桶名称
    private String endPoint; // 存储服务的终端节点
    private String downloadLink; // 文件下载链接的前缀
    private String region; // 存储桶所在地域/区域
}
