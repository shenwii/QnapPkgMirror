package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
public class SyncDateTime {
    @Id
    private String source;
    private LocalDateTime updateDateTime;
}
