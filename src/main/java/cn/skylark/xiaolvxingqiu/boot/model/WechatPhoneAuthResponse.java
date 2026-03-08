package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class WechatPhoneAuthResponse {
    private String phone;
    private String avatar;
    private String name;
    private String gender;
}
