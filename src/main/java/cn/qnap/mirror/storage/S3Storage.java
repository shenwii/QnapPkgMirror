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

@Component
public class S3Storage implements Storage {
    @Getter
    private String bucket;
    @Getter
    private String downloadLink;

    private S3Client s3;

    public S3Storage(Configure configure) {
        bucket = configure.getBucket();
        downloadLink = configure.getDownloadLink();
        AwsSessionCredentials awsCreds = AwsSessionCredentials.create(configure.getAccessKey(), configure.getSecretKey(), "");
        s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(configure.getEndPoint()))
                .region(Region.of(configure.getRegion()))
                .build();;
    }

    @Override
    public void writeFile(InputStream sourceSteam, String targetFile, Long length, String contentType) throws IOException {
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
//        var request = GetObjectRequest.builder()
//                .bucket(getBucketFullPath(filePath));
//        var responseTransformer = ResponseTransformer.toInputStream();
//        s3.getObject(request, responseTransformer);
        return null;
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        var request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(filePath)
                .build();
        s3.deleteObject(request);
    }

}
