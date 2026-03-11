package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class AiPlantCollection {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String imageUrl;
    private String recognizedImageUrl;
    private Double score;
    private String source;
    private String createdAt;
}
