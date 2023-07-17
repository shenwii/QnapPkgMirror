package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class Mirror {
    @Id
    private String id;              // ID
    private String source;          // 镜像识别符
    private String name;            // 包名称
    private String changeLog;       // 包的更新日志
    private String category;        // 包所属类别
    private String type;            // 包类型
    private String icon;            // 包图标
    private String description;     // 包描述
    private String fwVersion;       // 包适用的固件版本
    private List<Version> history = new ArrayList<>();   // 包历史版本列表
    private String maintainer;      // 包维护者
    private String developer;       // 包开发者
    private String language;        // 包语言
    private String tutorialLink;    // 包教程链接

    @Data
    @Builder
    public static class Version {
        private String version;         // 包版本号
        private String publishedDate;   // 包发布日期
        private Set<String> archList;   // 支持的架构列表
    }
}
