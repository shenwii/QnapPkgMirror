package cn.qnap.mirror.model.po;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Data
@Builder
public class Platform {
    @Id
    private String arch;
    private Set<String> machine;
}
