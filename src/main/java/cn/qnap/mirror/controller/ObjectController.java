package cn.qnap.mirror.controller;

import cn.qnap.mirror.service.ObjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@Controller
public class ObjectController {
    @Autowired
    private ObjectService objectService;
    @RequestMapping("/object/**")
    public void getObject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String object = path.replaceAll("^/object/", "");
        objectService.getObject(response, object);
    }
}
