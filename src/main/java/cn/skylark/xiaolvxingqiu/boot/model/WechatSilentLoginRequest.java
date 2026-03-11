package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WechatSilentLoginRequest {
    @NotBlank(message = "code cannot be empty")
    private String code;
}
