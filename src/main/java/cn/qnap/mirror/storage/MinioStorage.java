package cn.qnap.mirror.storage;

import cn.qnap.mirror.http.OKHttpClientBuilder;
import io.minio.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * MinioStorage类实现了Storage接口，用于与Minio存储服务交互。
 */
@Component("minioStorage")
public class MinioStorage implements Storage {
    @Getter
    private String bucket; // 存储桶名称
    @Getter
    private String downloadLink; // 文件下载链接的前缀
    private Configure configure; // 配置
    private static int BUFFER_LEN = 100 * 1024 * 1024; // 缓冲区大小
    private MinioClient minioClient;
    private boolean isRegister = false;

    /**
     * S3Storage构造函数，用于初始化S3存储客户端。
     *
     * @param configure 存储相关配置信息。
     */
    public MinioStorage(Configure configure) {
        bucket = configure.getBucket();
        downloadLink = configure.getDownloadLink();
        this.configure = configure;
    }

    protected void register() {
        if (isRegister) {
            return;
        }
        // 使用存储访问密钥和终端节点构建存储客户端
        minioClient = MinioClient.builder()
                .httpClient(OKHttpClientBuilder.buildOKHttpClient(true).build())
                .endpoint(configure.getEndPoint())
                .credentials(configure.getAccessKey(), configure.getSecretKey())
                .build();
        isRegister = true;
    }

    @Override
    public void writeFile(InputStream sourceSteam, String targetFile, Long length, String contentType) throws IOException {
        // 创建分块上传请求
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(targetFile)
                    .contentType(contentType)
                    .stream(sourceSteam, length, BUFFER_LEN)
                    .build());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public InputStream readFile(String filePath) throws IOException {
        // 获取文件的流
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        // 删除存储桶中的文件
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
