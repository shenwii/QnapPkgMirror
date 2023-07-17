package cn.qnap.mirror.repository;

import cn.qnap.mirror.model.po.Platform;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlatformRepository extends MongoRepository<Platform, String> {
}
