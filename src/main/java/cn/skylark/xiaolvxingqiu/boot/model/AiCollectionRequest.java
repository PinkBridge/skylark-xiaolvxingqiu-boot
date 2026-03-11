package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AiCollectionRequest {
    @NotBlank(message = "name cannot be empty")
    private String name;
    private String description;
    private String imageUrl;
    private String recognizedImageUrl;
    private Double score;
    private String source;
}
