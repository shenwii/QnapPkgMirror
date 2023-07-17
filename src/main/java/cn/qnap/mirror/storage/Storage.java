package cn.qnap.mirror.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Storage {
    void writeFile(InputStream sourceSteam, String targetFile, Long length, String contentType) throws IOException;

    InputStream readFile(String filePath) throws IOException;

    void deleteFile(String filePath) throws IOException;

    public String getDownloadLink();
}
