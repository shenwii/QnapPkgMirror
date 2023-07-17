package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class Mirror {
    private String id;
    private String source;
    private String name;
    private String changeLog;
    private String category;
    private String type;
    private String icon;
    private String description;
    private String fwVersion;
    private List<Version> history = new ArrayList<>();
    private String maintainer;
    private String developer;
    private String language;
    private String tutorialLink;

    @Data
    @Builder
    public static class Version {
        private String version;
        private String publishedDate;
        private Set<String> archList;
    }
}
