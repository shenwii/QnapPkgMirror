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
import java.util.ArrayList;
import java.util.List;

/**
 * S3Storage类实现了Storage接口，用于与S3存储服务交互。
 */
@Component
public class S3Storage implements Storage {
    @Getter
    private String bucket; // 存储桶名称
    @Getter
    private String downloadLink; // 文件下载链接的前缀
    private static int BUFFER_LEN = 4 * 1024 * 1024; // 缓冲区大小

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
        // 创建分块上传请求
        var createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(targetFile)
                .build();
        // 准备上传
        var response = s3.createMultipartUpload(createMultipartUploadRequest);
        // 获取上传ID
        var uploadId = response.uploadId();
        // 分片上传
        List<CompletedPart> partList = new ArrayList<>();
        while (length > 0) {
            long updateLength = length > BUFFER_LEN? BUFFER_LEN: length;
            var uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(targetFile)
                    .uploadId(uploadId)
                    .partNumber(partList.size() + 1).build();
            var etag = s3.uploadPart(uploadPartRequest, RequestBody.fromInputStream(sourceSteam, updateLength)).eTag();
            var completedPart = CompletedPart.builder().partNumber(partList.size() + 1).eTag(etag).build();
            partList.add(completedPart);
            length -= updateLength;
        }
        var completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(partList)
                .build();
        var completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(targetFile)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();
        // 完成上传
        s3.completeMultipartUpload(completeMultipartUploadRequest);
    }

    @Override
    public InputStream readFile(String filePath) throws IOException {
        // 获取S3文件的流
        var request = GetObjectRequest
                .builder()
                .bucket(bucket)
                .key(filePath)
                .build();
        return s3.getObject(request);
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
