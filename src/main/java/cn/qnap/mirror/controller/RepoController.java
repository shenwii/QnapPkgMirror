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

    @RequestMapping("/repo.xml")
    public String getRepo(HttpServletResponse response) throws IOException {
        return repoService.getRepo(response);
    }
}