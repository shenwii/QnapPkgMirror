package cn.qnap.mirror.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ObjectService 接口定义了获取文件的方法。
 */
@Service
public interface ObjectService {

    void getObject(HttpServletResponse response, String object) throws IOException;
}
