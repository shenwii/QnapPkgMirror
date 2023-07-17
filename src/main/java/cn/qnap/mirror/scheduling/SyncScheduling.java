package cn.qnap.mirror.scheduling;

import cn.qnap.mirror.http.OKHttpClientBuilder;
import cn.qnap.mirror.model.co.SyncCo;
import cn.qnap.mirror.model.po.Mirror;
import cn.qnap.mirror.model.po.Platform;
import cn.qnap.mirror.model.po.SyncDateTime;
import cn.qnap.mirror.model.xml.PackageXmlRoot;
import cn.qnap.mirror.repository.MirrorRepository;
import cn.qnap.mirror.repository.PlatformRepository;
import cn.qnap.mirror.repository.SyncDateTimeRepository;
import cn.qnap.mirror.storage.S3Storage;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Setter;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "qnap")
@Component
public class SyncScheduling implements SchedulingConfigurer {

    @Setter
    private List<SyncCo> syncList;

    @Autowired
    private MirrorRepository mirrorRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private SyncDateTimeRepository syncDateTimeRepository;

    @Autowired
    private S3Storage s3Storage;
    private final Map<String, String>CONTENT_MAP = new HashMap<>() {
        {
            put("gif", "image/gif");
            put("png", "image/png");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
        }
    };

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        syncList.forEach(syncCo -> {
            try {
                taskRegistrar.addCronTask(new SyncJob(syncCo), syncCo.getCron());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
    }

    private class SyncJob implements Runnable {
        final private SyncCo syncCo;
        final private String s3BasePath;

        public SyncJob(SyncCo syncCo) {
            this.syncCo = syncCo;
            s3BasePath = syncCo.getId() + "/";
        }

        @Override
        public void run() {
            var okHttpClient = OKHttpClientBuilder.buildOKHttpClient(syncCo.isVerifySsl())
                    .build();
            var request = new Request.Builder().url(syncCo.getUrl()).build();
            String responseStr;
            try (var response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return;
                }
                responseStr = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            responseStr = responseStr.replaceAll("<changeLog></changeLog>", "");
            PackageXmlRoot packageXmlRoot;
            try {
                var xmlMapper = new XmlMapper();
                packageXmlRoot = xmlMapper.readValue(responseStr, PackageXmlRoot.class);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            var mirrorMap = getMirror(syncCo.getId());
            var platformMap = getPlatform();
            packageXmlRoot.getItems().forEach(item -> {
                Mirror mirrorDto;
                if (mirrorMap.containsKey(item.getName())) {
                    mirrorDto = mirrorMap.get(item.getName());
                } else {
                    mirrorDto = new Mirror();
                }
                mirrorDto.setSource(syncCo.getId());
                mirrorDto.setName(item.getName());
                mirrorDto.setChangeLog(item.getChangeLog());
                mirrorDto.setCategory(item.getCategory());
                mirrorDto.setType(item.getType());
                mirrorDto.setDescription(item.getDescription());
                mirrorDto.setFwVersion(item.getFwVersion());
                String iconUrl = item.getIcon80().equals("")? item.getIcon100(): item.getIcon80();
                var xmlPlatformMap = parsePlatforms(item.getName(), item.getVersion(), item.getPlatforms());
                String currentVersion = mirrorDto.getHistory().size() == 0? null: mirrorDto.getHistory().get(0).getVersion();
                if (!item.getVersion().equals(currentVersion)) {
                    var version = Mirror.Version.builder()
                            .version(item.getVersion())
                            .publishedDate(item.getPublishedDate())
                            .archList(xmlPlatformMap.keySet())
                            .build();
                    for(Map<String, Object> map: xmlPlatformMap.values()) {
                        String url = (String) map.get("url");
                        try {
                            uploadFile(url, s3BasePath
                                    + "packages/"
                                    + item.getName()
                                    + "_"
                                    + item.getVersion()
                                    + "_"
                                    + map.get("arch")
                                    + ".qpkg");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        syncDateTimeRepository.save(SyncDateTime.builder()
                                        .source(syncCo.getId())
                                        .updateDateTime(LocalDateTime.now())
                                        .build());
                    }
                    if (!iconUrl.equals("")) {
                        String icon = iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
                        try {
                            uploadFile(iconUrl, s3BasePath + "icon/" + icon);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        mirrorDto.setIcon(icon);
                    }
                    mirrorDto.getHistory().add(0, version);
                }
                xmlPlatformMap.forEach((arch, map) -> {
                    if (platformMap.containsKey(arch)) {
                        platformMap.get(arch).getMachine().addAll((Set<String>) map.get("machines"));
                    } else {
                        platformMap.put(arch, Platform.builder()
                                        .arch(arch)
                                        .machine((Set<String>) map.get("machines"))
                                        .build());
                    }
                });
                mirrorDto.setMaintainer(item.getMaintainer());
                mirrorDto.setDeveloper(item.getDeveloper());
                mirrorDto.setLanguage(item.getLanguage());
                mirrorDto.setTutorialLink(item.getTutorialLink());
                mirrorRepository.save(mirrorDto);
            });
            platformRepository.saveAll(platformMap.values());
        }

        private Map<String, Mirror> getMirror(String source) {
            Map<String, Mirror> result = new HashMap<>();
            mirrorRepository.findBySource(source).forEach(mirror -> {
                result.put(mirror.getName(), mirror);
            });
            return result;
        }
        private Map<String, Platform> getPlatform() {
            Map<String, Platform> result = new HashMap<>();
            platformRepository.findAll().forEach(platform -> {
                result.put(platform.getArch(), platform);
            });
            return result;
        }

        private Map<String, Map<String, Object>> parsePlatforms(String name, String version, List<PackageXmlRoot.Platform> platformList) {
            Map<String, Map<String, Object>> result = new HashMap<>();
            platformList.forEach(platform -> {
                var fileName = platform.getLocation().substring(platform.getLocation().lastIndexOf("/") + 1);
                var arch = fileName.substring(name.length() + version.length() + 2, fileName.length() - 5);
                if (!result.containsKey(arch)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("arch", arch);
                    map.put("url", platform.getLocation());
                    map.put("machines", new HashSet<String>());
                    result.put(arch, map);
                }
                ((Set<String>) result.get(arch).get("machines")).add(platform.getPlatformID());
            });
            return result;
        }

        private void uploadFile(String url, String filePath) throws IOException {
            var okHttpClient = OKHttpClientBuilder.buildOKHttpClient(syncCo.isVerifySsl())
                    .build();
            var request = new Request.Builder().url(url).build();
            try (var response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("download file failed");
                }
                String fileExt = url.substring(url.lastIndexOf(".") + 1);
                s3Storage.writeFile(response.body().byteStream(), filePath, response.body().contentLength(), getContentType(fileExt));
            }
        }
        private String getContentType(String fileExt) {
            final String defaultContentType = "application/octet-stream";
            if(fileExt == null)
                return defaultContentType;
            String fileExtLow = fileExt.toLowerCase();
            return CONTENT_MAP.getOrDefault(fileExtLow, defaultContentType);
        }
    }
}
