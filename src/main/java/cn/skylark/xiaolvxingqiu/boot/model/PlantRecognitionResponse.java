package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlantRecognitionResponse {
    private String name;
    private Double score;
    private String description;
    private String imageUrl;
    private List<PlantRecognitionItem> candidates = new ArrayList<>();
}
