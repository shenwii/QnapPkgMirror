package cn.qnap.mirror.model.co;

import lombok.Data;

@Data
public class SyncCo {
    private String id;          // 同步任务的唯一标识符
    private String url;         // 同步的URL地址
    private String cron;        // 定时任务的CRON表达式，用于指定同步的时间规则
    private boolean verifySsl;  // 是否验证SSL证书链
}
