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
import cn.qnap.mirror.storage.Storage;
import cn.qnap.mirror.storage.StorageFactory;
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
import java.util.regex.Pattern;

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
    private StorageFactory storageFactory;

    private Storage storage;

    private final Map<String, String> CONTENT_MAP = new HashMap<>() { //图片的媒体类型
        {
            put("gif", "image/gif");
            put("png", "image/png");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
        }
    };

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        storage = storageFactory.getStorage();
        // 配置定时任务
        syncList.forEach(syncCo -> {
            try {
                taskRegistrar.addCronTask(new SyncJob(syncCo), syncCo.getCron());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 同步任务
     */
    private class SyncJob implements Runnable {
        final private SyncCo syncCo;
        final private String s3BasePath;

        public SyncJob(SyncCo syncCo) {
            this.syncCo = syncCo;
            s3BasePath = syncCo.getId() + "/";
        }

        /**
         * 同步任务
         */
        @Override
        public void run() {
            // 创建OkHttpClient，并发送HTTP请求获取镜像信息
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
            // 解析XML格式的镜像信息
            responseStr = responseStr.replaceAll("<changeLog></changeLog>", ""); //替换掉一个空的changeLog标签
            PackageXmlRoot packageXmlRoot;
            try {
                var xmlMapper = new XmlMapper();
                packageXmlRoot = xmlMapper.readValue(responseStr, PackageXmlRoot.class);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            // 获取当前的镜像中存的包列表
            var mirrorMap = getMirror(syncCo.getId());
            // 获取构架和平台列表
            var platformMap = getPlatform(packageXmlRoot);
            // 保存构架和平台信息
            platformRepository.saveAll(platformMap.values());
            packageXmlRoot.getItems().forEach(item -> { //循环xml中的信息
                Mirror mirrorDto;
                // 如果该包已存在，则取出，否则新建一个
                if (mirrorMap.containsKey(item.getName())) {
                    mirrorDto = mirrorMap.get(item.getName());
                } else {
                    mirrorDto = new Mirror();
                }
                // 设置包信息
                mirrorDto.setSource(syncCo.getId());
                mirrorDto.setName(item.getName());
                mirrorDto.setChangeLog(item.getChangeLog());
                mirrorDto.setCategory(item.getCategory());
                mirrorDto.setType(item.getType());
                mirrorDto.setDescription(item.getDescription());
                mirrorDto.setFwVersion(item.getFwVersion());
                String iconUrl = item.getIcon80().equals("") ? item.getIcon100() : item.getIcon80();
                // 解析构架和平台的关系
                var xmlPlatformMap = parsePlatforms(platformMap, item.getPlatforms());
                // 取出当前的版本信息
                String currentVersion = mirrorDto.getHistory().size() == 0 ? null : mirrorDto.getHistory().get(0).getVersion();
                // 如果版本不一致，则更新版本
                if (!item.getVersion().equals(currentVersion)) {
                    var version = Mirror.Version.builder()
                            .version(item.getVersion())
                            .publishedDate(item.getPublishedDate())
                            .archList(xmlPlatformMap.keySet())
                            .build();
                    xmlPlatformMap.forEach((arch, url) -> {
                        // 将每个包上传到S3服务器
                        try {
                            uploadFile(url, s3BasePath
                                    + "packages/"
                                    + item.getName()
                                    + "_"
                                    + item.getVersion()
                                    + "_"
                                    + arch
                                    + ".qpkg");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        // 记录同步日期时间
                        syncDateTimeRepository.save(SyncDateTime.builder()
                                .source(syncCo.getId())
                                .updateDateTime(LocalDateTime.now())
                                .build());
                    });
                    // 上传图标文件
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
                    // 将最新版本插入到第一条
                    mirrorDto.getHistory().add(0, version);
                }
                // 保存镜像信息
                mirrorDto.setMaintainer(item.getMaintainer());
                mirrorDto.setDeveloper(item.getDeveloper());
                mirrorDto.setLanguage(item.getLanguage());
                mirrorDto.setTutorialLink(item.getTutorialLink());
                mirrorRepository.save(mirrorDto);
            });
        }

        /**
         * 根据镜像识别符来获取镜像包信息
         * @param source 镜像识别符
         * @return 镜像包信息Map
         */
        private Map<String, Mirror> getMirror(String source) {
            Map<String, Mirror> result = new HashMap<>();
            // 查询数据库获取同步源对应的镜像列表
            mirrorRepository.findBySource(source).forEach(mirror -> {
                result.put(mirror.getName(), mirror);
            });
            return result;
        }

        /**
         * 获取构架和平台关系
         * @return 构架和平台Map
         */
        private Map<String, Platform> getPlatform(PackageXmlRoot packageXmlRoot) {
            Pattern archPattern = Pattern.compile(".*_((arm|x86).+)$");
            Map<String, Platform> result = new HashMap<>();
            platformRepository.findAll().forEach(platform -> {
                result.put(platform.getArch(), platform);
            });
            packageXmlRoot.getItems().forEach(item -> {
                item.getPlatforms().forEach(platform -> {
                    // 截取最后一位/以后的文件名
                    var fileName = platform.getLocation().substring(platform.getLocation().lastIndexOf("/") + 1);
                    // 从文件名中解析出构架
                    String arch;
                    try {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        var match = archPattern.matcher(fileName);
                        if (!match.matches()) {
                            return;
                        }
                        arch = match.group(1).toLowerCase().replaceAll("_", "-");
                    } catch (Throwable e) {
                        return;
                    }
                    Platform platformTmp;
                    if (!result.containsKey(arch)) {
                        platformTmp = Platform.builder()
                                .arch(arch)
                                .machine(new HashSet<String>())
                                .build();
                        result.put(arch, platformTmp);
                    }
                    result.get(arch).getMachine().add(platform.getPlatformID());
                });
            });
            return result;
        }

        /**
         * 解析出构架和平台的关系
         * @param platformMap 构架和平台关系
         * @param platformList 平台列表
         * @return 解析好的Map
         */
        private Map<String, String> parsePlatforms(Map<String, Platform> platformMap, List<PackageXmlRoot.Platform> platformList) {
            Map<String, String> result = new HashMap<>();
            platformList.forEach(platform -> {
                platformMap.forEach((arch, platformTmp) -> {
                    if (platformTmp.getMachine().contains(platform.getPlatformID()) && !result.containsKey(arch)) {
                        result.put(arch, platform.getLocation());
                    }
                });
            });
            return result;
        }

        /**
         * 上传文件
         * @param url 文件的URL
         * @param filePath 上传文件的路径
         * @throws IOException
         */
        private void uploadFile(String url, String filePath) throws IOException {
            var okHttpClient = OKHttpClientBuilder.buildOKHttpClient(syncCo.isVerifySsl())
                    .build();
            var request = new Request.Builder().url(url).build();
            try (var response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("download file failed");
                }
                String fileExt = url.substring(url.lastIndexOf(".") + 1);
                // 上传文件到存储
                storage.writeFile(response.body().byteStream(), filePath, response.body().contentLength(), getContentType(fileExt));
            }
        }

        /**
         * 根据文件后缀或者文件媒体类型
         * @param fileExt 文件后缀
         * @return 文件媒体类型
         */
        private String getContentType(String fileExt) {
            final String defaultContentType = "application/octet-stream";
            if (fileExt == null)
                return defaultContentType;
            String fileExtLow = fileExt.toLowerCase();
            return CONTENT_MAP.getOrDefault(fileExtLow, defaultContentType);
        }
    }
}
