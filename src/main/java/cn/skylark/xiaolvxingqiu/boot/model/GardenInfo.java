package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GardenInfo {

    @NotBlank(message = "garden title cannot be empty")
    private String title;

    @NotBlank(message = "garden date cannot be empty")
    private String subTitle;

    private String thumb;
    private String image;
    private String description;
}
