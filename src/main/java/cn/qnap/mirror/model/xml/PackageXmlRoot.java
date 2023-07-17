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
    private String cacheCheck;

    @JacksonXmlProperty(localName = "item")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Item> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JacksonXmlProperty
        private String name;
        @JacksonXmlProperty
        private String internalName;
        @JacksonXmlProperty
        private String changeLog;
        @JacksonXmlProperty
        private String category;
        @JacksonXmlProperty
        private String type;
        @JacksonXmlProperty
        private String icon80;
        @JacksonXmlProperty
        private String icon100;
        @JacksonXmlProperty
        private String description;
        @JacksonXmlProperty
        private String fwVersion;
        @JacksonXmlProperty
        private String version;
        @JacksonXmlProperty(localName = "platform")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Platform> platforms;
        @JacksonXmlProperty
        private String publishedDate;
        @JacksonXmlProperty
        private String maintainer;
        @JacksonXmlProperty
        private String developer;
        @JacksonXmlProperty
        private String forumLink;
        @JacksonXmlProperty
        private String language;
        @JacksonXmlProperty
        private String snapshot;
        @JacksonXmlProperty
        private String bannerImg;
        @JacksonXmlProperty
        private String tutorialLink;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Platform {
        @JacksonXmlProperty
        private String platformID;
        @JacksonXmlProperty
        private String location;
    }
}
