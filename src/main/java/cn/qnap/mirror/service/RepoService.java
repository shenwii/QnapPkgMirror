package cn.qnap.mirror.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * RepoService 接口定义了获取仓库XML数据的方法。
 */
@Service
public interface RepoService {

    /**
     * 获取仓库XML数据并写入HttpServletResponse响应对象。
     *
     * @param response HttpServletResponse对象，用于返回仓库XML数据。
     * @return 返回空字符串。
     * @throws IOException 当获取仓库XML数据或写入响应对象时发生I/O异常。
     */
    String getRepo(HttpServletResponse response) throws IOException;
}
