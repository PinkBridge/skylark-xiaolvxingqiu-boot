package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class UserWechatIdentity {
    private Long id;
    private Long userId;
    private String openid;
    private String unionid;
}
