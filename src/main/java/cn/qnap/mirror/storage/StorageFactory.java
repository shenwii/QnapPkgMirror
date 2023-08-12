package cn.qnap.mirror.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component()
public class StorageFactory {

    @Autowired
    private S3Storage s3Storage;
    @Autowired
    private MinioStorage minioStorage;
    @Autowired
    private Configure configure;

    public Storage getStorage() {
        if (configure.getType().equals("s3")) {
            s3Storage.register();
            return s3Storage;
        }
        if (configure.getType().equals("minio")) {
            minioStorage.register();
            return minioStorage;
        }
        return null;
    }
}
