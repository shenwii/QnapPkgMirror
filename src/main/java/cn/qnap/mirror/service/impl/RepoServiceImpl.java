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
        // 创建PackageXmlRoot对象，用于生成XML数据
        var packageXmlRoot = new PackageXmlRoot();
        packageXmlRoot.setCacheCheck(getLastUpdateDateTime());
        packageXmlRoot.setItems(generateItemList());

        // 设置HTTP响应头，指定返回的是XML格式数据
        response.reset();
        response.setContentType("application/xml");
        response.setCharacterEncoding(FILE_ENCODE);
        response.setHeader("Content-Disposition", "attachment;filename=" + FILE_NAME);

        // 使用XmlMapper将PackageXmlRoot对象转换为XML字符串，并写入响应输出流
        var xmlMapper = new XmlMapper();
        try (var write = new OutputStreamWriter(response.getOutputStream(), FILE_ENCODE)) {
            xmlMapper.writerWithDefaultPrettyPrinter().writeValue(write, packageXmlRoot);
        }
        return "";
    }

    /**
     * 获取最后一次更新日期和时间
     * @return 最后一次更新日期和时间
     */
    private String getLastUpdateDateTime() {
        // 获取最后同步日期时间，并格式化为yyyyMMddHHmm的字符串
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

    /**
     * 生成item列表
     * @return item列表
     */
    private List<PackageXmlRoot.Item> generateItemList() {
        // 生成镜像列表项
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

    /**
     * 生成平台列表
     * @param name 包名
     * @param version 包版本
     * @param archSet 构架列表
     * @return 平台列表
     */
    private List<PackageXmlRoot.Platform> generatePlatformList(String name, String version, Set<String> archSet) {
        // 生成平台列表项
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

    /**
     * 获取构架和平台列表
     * @return 构架和平台Map
     */
    private Map<String, Platform> getPlatform() {
        // 获取平台信息并缓存
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
