package cn.qnap.mirror.model.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JacksonXmlRootElement(localName = "plugins")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PackageXmlRoot {

    @JacksonXmlProperty(localName = "cachechk")
    private String cacheCheck;          // 缓存检查

    @JacksonXmlProperty(localName = "item")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Item> items;           // 项目列表

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JacksonXmlProperty
        private String name;                // 项目名称
        @JacksonXmlProperty
        private String internalName;        // 内部名称
        @JacksonXmlProperty
        private String changeLog;           // 更新日志
        @JacksonXmlProperty
        private String category;            // 项目类别
        @JacksonXmlProperty
        private String type;                // 项目类型
        @JacksonXmlProperty
        private String icon80;              // 80x80像素图标地址
        @JacksonXmlProperty
        private String icon100;             // 100x100像素图标地址
        @JacksonXmlProperty
        private String description;         // 项目描述
        @JacksonXmlProperty
        private String fwVersion;           // 适用的固件版本
        @JacksonXmlProperty
        private String version;             // 项目版本
        @JacksonXmlProperty(localName = "platform")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Platform> platforms;   // 支持的平台列表
        @JacksonXmlProperty
        private String publishedDate;       // 发布日期
        @JacksonXmlProperty
        private String maintainer;          // 维护者
        @JacksonXmlProperty
        private String developer;           // 开发者
        @JacksonXmlProperty
        private String forumLink;           // 论坛链接
        @JacksonXmlProperty
        private String language;            // 项目开发语言
        @JacksonXmlProperty
        private String snapshot;            // 快照
        @JacksonXmlProperty
        private String bannerImg;           // 横幅图片
        @JacksonXmlProperty
        private String tutorialLink;        // 教程链接
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Platform {
        @JacksonXmlProperty
        private String platformID;      // 平台ID
        @JacksonXmlProperty
        private String location;        // 位置
    }
}
