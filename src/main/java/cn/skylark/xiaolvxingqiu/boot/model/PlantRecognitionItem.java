package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class PlantRecognitionItem {
    private String name;
    private Double score;
    private String description;
    private String imageUrl;
}
