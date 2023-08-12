package cn.qnap.mirror.service.impl;

import cn.qnap.mirror.service.ObjectService;
import cn.qnap.mirror.storage.Storage;
import cn.qnap.mirror.storage.StorageFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service("objectService")
public class ObjectServiceImpl implements ObjectService {
    @Autowired
    private StorageFactory storageFactory;
    private static int BUFFER_LEN = 4 * 1024 * 1024; // 缓冲区大小
    private final Map<String, String> CONTENT_MAP = new HashMap<>() { //图片的媒体类型
        {
            put("gif", "image/gif");
            put("png", "image/png");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
        }
    };

    @Override
    public void getObject(HttpServletResponse response, String object) throws IOException {
        byte[] buffer = new byte[BUFFER_LEN];
        int read;
        String fileName = object.substring(object.lastIndexOf("/") + 1);
        Storage storage = storageFactory.getStorage();
        response.reset();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(getContentType(fileName.substring(fileName.lastIndexOf(".") + 1)));
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setCharacterEncoding("utf-8");

        OutputStream os = response.getOutputStream();
        InputStream is = storage.readFile(object);
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
            os.flush();
        }
    }

    /**
     * 根据文件后缀或者文件媒体类型
     * @param fileExt 文件后缀
     * @return 文件媒体类型
     */
    private String getContentType(String fileExt) {
        final String defaultContentType = "application/octet-stream";
        if (fileExt == null)
            return defaultContentType;
        String fileExtLow = fileExt.toLowerCase();
        return CONTENT_MAP.getOrDefault(fileExtLow, defaultContentType);
    }
}
