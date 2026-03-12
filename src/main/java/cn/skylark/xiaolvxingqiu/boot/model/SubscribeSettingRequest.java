package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class SubscribeSettingRequest {
    private Boolean enabled;
    private String pushTime;
    private String authStatus;
}
