package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GardenUpsertRequest {

    @NotBlank(message = "garden name cannot be empty")
    private String name;

    @NotBlank(message = "garden establishedDate cannot be empty")
    private String establishedDate;

    private String thumbUrl;
    private String coverUrl;
    private String description;
}
