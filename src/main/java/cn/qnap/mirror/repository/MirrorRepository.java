package cn.qnap.mirror.repository;

import cn.qnap.mirror.model.po.Mirror;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MirrorRepository extends MongoRepository<Mirror, String> {
    List<Mirror> findBySource(String source);
}
