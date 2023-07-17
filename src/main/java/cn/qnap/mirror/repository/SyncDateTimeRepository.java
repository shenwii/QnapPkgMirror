package cn.qnap.mirror.repository;

import cn.qnap.mirror.model.po.SyncDateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SyncDateTimeRepository extends MongoRepository<SyncDateTime, String> {
}
