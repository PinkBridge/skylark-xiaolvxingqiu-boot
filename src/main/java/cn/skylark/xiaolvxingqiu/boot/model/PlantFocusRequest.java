package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PlantFocusRequest {
    @NotBlank(message = "reason cannot be empty")
    private String reason;

    @NotBlank(message = "photoUrl cannot be empty")
    private String photoUrl;
}
