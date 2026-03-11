package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class WechatSilentLoginResponse {
    private Long userId;
    private boolean newUser;
}
