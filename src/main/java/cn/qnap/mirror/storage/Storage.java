package cn.qnap.mirror.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Storage接口定义了与存储服务交互的方法。
 */
public interface Storage {
    /**
     * 将输入流中的数据写入存储服务中指定的文件。
     *
     * @param sourceSteam 输入流，包含待写入的数据。
     * @param targetFile 存储服务中的目标文件路径。
     * @param length     待写入数据的长度。
     * @param contentType 待写入数据的内容类型。
     * @throws IOException 当写入存储服务时发生I/O异常。
     */
    void writeFile(InputStream sourceSteam, String targetFile, Long length, String contentType) throws IOException;

    /**
     * 从存储服务中读取指定文件，并返回对应的输入流。
     *
     * @param filePath 存储服务中的目标文件路径。
     * @return 文件对应的输入流。
     * @throws IOException 当读取存储服务中的文件时发生I/O异常。
     */
    InputStream readFile(String filePath) throws IOException;

    /**
     * 删除存储服务中指定的文件。
     *
     * @param filePath 存储服务中的目标文件路径。
     * @throws IOException 当删除存储服务中的文件时发生I/O异常。
     */
    void deleteFile(String filePath) throws IOException;

    /**
     * 获取文件下载链接的前缀。
     *
     * @return 文件下载链接的前缀。
     */
    String getDownloadLink();
}
