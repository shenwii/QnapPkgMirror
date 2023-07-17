package cn.qnap.mirror.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface RepoService {

    String getRepo(HttpServletResponse response) throws IOException;
}
