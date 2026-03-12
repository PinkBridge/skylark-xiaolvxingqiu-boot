package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserSubscribeSetting {
    private Long userId;
    private Boolean enabled;
    private String pushTime;
    private String templateId;
    private String authStatus;
    private LocalDateTime lastAuthAt;
    private LocalDate lastPushDate;
    private String lastPushStatus;
    private String lastPushResult;
}
