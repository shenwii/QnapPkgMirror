package cn.qnap.mirror.controller;

import cn.qnap.mirror.service.RepoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class RepoController {

    @Autowired
    private RepoService repoService;

    /**
     * 请求获取repo.xml文件的处理方法
     * 
     * @param response HttpServletResponse对象，用于返回HTTP响应
     * @return 返回repoService处理后的repo.xml文件内容
     * @throws IOException 当处理过程中发生I/O异常时抛出
     */
    @RequestMapping("/repo.xml")
    public String getRepo(HttpServletResponse response) throws IOException {
        return repoService.getRepo(response);
    }
}
