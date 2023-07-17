package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
public class SyncDateTime {
    @Id
    private String source;                      // 同步源的标识符
    private LocalDateTime updateDateTime;      // 上次同步的日期和时间
}
