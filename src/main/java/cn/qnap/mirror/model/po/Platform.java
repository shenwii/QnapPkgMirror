package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Data
@Builder
public class Platform {
    @Id
    private String arch;            // 平台的架构标识符
    private Set<String> machine;    // 支持的机器类型集合
}
