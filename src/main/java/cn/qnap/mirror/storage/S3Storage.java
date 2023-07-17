package cn.qnap.mirror.storage;

import com.mongodb.internal.bulk.DeleteRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * S3Storage类实现了Storage接口，用于与S3存储服务交互。
 */
@Component
public class S3Storage implements Storage {
    @Getter
    private String bucket; // 存储桶名称
    @Getter
    private String downloadLink; // 文件下载链接的前缀

    private S3Client s3;

    /**
     * S3Storage构造函数，用于初始化S3存储客户端。
     *
     * @param configure 存储相关配置信息。
     */
    public S3Storage(Configure configure) {
        bucket = configure.getBucket();
        downloadLink = configure.getDownloadLink();
        // 使用存储访问密钥和终端节点构建S3存储客户端
        AwsSessionCredentials awsCreds = AwsSessionCredentials.create(configure.getAccessKey(), configure.getSecretKey(), "");
        s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(configure.getEndPoint()))
                .region(Region.of(configure.getRegion()))
                .build();
    }

    @Override
    public void writeFile(InputStream sourceSteam, String targetFile, Long length, String contentType) throws IOException {
        // 将数据写入S3存储桶中
        var request = PutObjectRequest.builder()
                .bucket(bucket)
                .contentType(contentType)
                .key(targetFile)
                .contentLength(length)
                .build();
        var requestBody = RequestBody.fromInputStream(sourceSteam, length);
        s3.putObject(request, requestBody);
    }

    @Override
    public InputStream readFile(String filePath) throws IOException {
        // TODO: 实现从S3存储桶中读取文件，并返回InputStream对象
        // 可以参考以下代码示例
        /*
        var request = GetObjectRequest.builder()
                .bucket(getBucketFullPath(filePath));
        var responseTransformer = ResponseTransformer.toInputStream();
        return s3.getObject(request, responseTransformer);
        */
        return null;
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        // 删除S3存储桶中的文件
        var request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(filePath)
                .build();
        s3.deleteObject(request);
    }
}
