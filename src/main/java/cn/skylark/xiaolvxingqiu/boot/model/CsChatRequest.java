package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CsChatRequest {

    private String sessionId;

    @NotBlank(message = "message cannot be empty")
    private String message;
}
