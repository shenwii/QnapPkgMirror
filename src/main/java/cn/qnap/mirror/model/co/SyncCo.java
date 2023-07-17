package cn.qnap.mirror.model.co;

import lombok.Data;

@Data
public class SyncCo {
    private String id;
    private String url;
    private String cron;
    private boolean verifySsl;
}
