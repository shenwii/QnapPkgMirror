package cn.qnap.mirror.repository;

import cn.qnap.mirror.model.po.Mirror;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MirrorRepository extends MongoRepository<Mirror, String> {
    /**
     * 根据同步源地址查找对应的镜像列表
     *
     * @param source 同步源地址
     * @return 匹配的镜像列表
     */
    List<Mirror> findBySource(String source);
}
