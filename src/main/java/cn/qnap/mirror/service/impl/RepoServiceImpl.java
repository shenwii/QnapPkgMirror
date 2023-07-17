package cn.qnap.mirror.service.impl;

import cn.qnap.mirror.model.po.Mirror;
import cn.qnap.mirror.model.po.Platform;
import cn.qnap.mirror.model.xml.PackageXmlRoot;
import cn.qnap.mirror.repository.MirrorRepository;
import cn.qnap.mirror.repository.PlatformRepository;
import cn.qnap.mirror.repository.SyncDateTimeRepository;
import cn.qnap.mirror.service.RepoService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("repoService")
public class RepoServiceImpl implements RepoService {
    @Value("${qnap.source}")
    @Setter
    private String source;
    @Value("${storage.download-link}")
    @Setter
    private String downloadLink;

    private final static String FILE_NAME = "repo.xml";
    private final static String FILE_ENCODE = "utf-8";

    @Autowired
    private MirrorRepository mirrorRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SyncDateTimeRepository syncDateTimeRepository;

    private Map<String, Platform> platformMap = null;

    @Override
    public String getRepo(HttpServletResponse response) throws IOException {
        var packageXmlRoot = new PackageXmlRoot();
        packageXmlRoot.setCacheCheck(getLastUpdateDateTime());
        packageXmlRoot.setItems(generateItemList());

        response.reset();
        response.setContentType("application/xml");
        response.setCharacterEncoding(FILE_ENCODE);
        response.setHeader("Content-Disposition", "attachment;filename=" + FILE_NAME );

        var xmlMapper = new XmlMapper();
        try(var write = new OutputStreamWriter(response.getOutputStream(), FILE_ENCODE)) {
            xmlMapper.writerWithDefaultPrettyPrinter().writeValue(write, packageXmlRoot);
        }
        return "";
    }

    private String getLastUpdateDateTime() {
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        var syncDateTimeOption = syncDateTimeRepository.findById(source);
        LocalDateTime updateDateTime;
        if (syncDateTimeOption.isPresent()) {
            updateDateTime = syncDateTimeOption.get().getUpdateDateTime();
        } else {
            updateDateTime = LocalDateTime.now();
        }
        return updateDateTime.format(dateTimeFormatter);
    }

    private List<PackageXmlRoot.Item> generateItemList() {
        List<PackageXmlRoot.Item> itemList = new ArrayList<>();
        mirrorRepository.findBySource(source).forEach(mirror -> {
            Mirror.Version version = mirror.getHistory().get(0);
            PackageXmlRoot.Item item = new PackageXmlRoot.Item();
            item.setName(mirror.getName());
            item.setInternalName(mirror.getName());
            item.setChangeLog(mirror.getChangeLog());
            item.setCategory(mirror.getCategory());
            item.setType(mirror.getType());
            item.setIcon80(downloadLink + source + "/icon/" + mirror.getIcon());
            item.setIcon100("");
            item.setDescription(mirror.getDescription());
            item.setFwVersion(mirror.getFwVersion());
            item.setVersion(version.getVersion());
            item.setPlatforms(generatePlatformList(mirror.getName(), version.getVersion(), version.getArchList()));
            item.setPublishedDate(version.getPublishedDate());
            item.setMaintainer(mirror.getMaintainer());
            item.setDeveloper(mirror.getDeveloper());
            item.setForumLink("");
            item.setLanguage(mirror.getLanguage());
            item.setSnapshot("");
            item.setBannerImg("");
            item.setTutorialLink(mirror.getTutorialLink());
            itemList.add(item);
        });
        return itemList;
    }
    private List<PackageXmlRoot.Platform> generatePlatformList(String name, String version, Set<String> archSet) {
        List<PackageXmlRoot.Platform> platformList = new ArrayList<>();
        Map<String, Platform> platformMap = getPlatform();
        archSet.forEach(arch -> {
            if (!platformMap.containsKey(arch)) {
                return;
            }
            platformMap.get(arch).getMachine().forEach(machine -> {
                PackageXmlRoot.Platform platform = new PackageXmlRoot.Platform();
                platform.setPlatformID(machine);
                platform.setLocation(downloadLink
                        + source
                        + "/packages/"
                        + name
                        + "_"
                        + version
                        + "_"
                        + arch
                        + ".qpkg");
                platformList.add(platform);
            });
        });
        return platformList;
    }

    private Map<String, Platform> getPlatform() {
        if (platformMap != null) {
            return platformMap;
        }
        platformMap = new HashMap<>();
        platformRepository.findAll().forEach(platform -> {
            platformMap.put(platform.getArch(), platform);
        });
        return platformMap;
    }
}
