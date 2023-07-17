package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class Mirror {
    private String id;              // 镜像的唯一标识符
    private String source;          // 镜像源地址
    private String name;            // 镜像名称
    private String changeLog;       // 镜像的更新日志
    private String category;        // 镜像所属类别
    private String type;            // 镜像类型
    private String icon;            // 镜像图标地址
    private String description;     // 镜像描述
    private String fwVersion;       // 镜像适用的固件版本
    private List<Version> history = new ArrayList<>();   // 镜像历史版本列表
    private String maintainer;      // 镜像维护者
    private String developer;       // 镜像开发者
    private String language;        // 镜像开发语言
    private String tutorialLink;    // 镜像教程链接

    @Data
    @Builder
    public static class Version {
        private String version;         // 镜像版本号
        private String publishedDate;   // 镜像发布日期
        private Set<String> archList;   // 支持的架构列表
    }
}
