package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WechatPhoneAuthRequest {
    @NotBlank(message = "code cannot be empty")
    private String code;
    @NotBlank(message = "encryptedData cannot be empty")
    private String encryptedData;
    @NotBlank(message = "iv cannot be empty")
    private String iv;
}
