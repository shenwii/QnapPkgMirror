package cn.qnap.mirror;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application类是整个应用程序的入口点。
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    /**
     * main方法用于启动Spring Boot应用。
     *
     * @param args 启动参数。
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
