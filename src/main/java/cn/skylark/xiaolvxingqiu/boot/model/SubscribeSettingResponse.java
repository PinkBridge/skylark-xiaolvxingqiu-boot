package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class SubscribeSettingResponse {
    private Boolean enabled;
    private String pushTime;
    private String templateId;
    private String authStatus;
}
