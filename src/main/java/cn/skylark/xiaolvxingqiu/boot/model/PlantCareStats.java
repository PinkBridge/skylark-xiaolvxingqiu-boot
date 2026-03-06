package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlantCareStats {
    private Long totalCareCount;
    private Long photoCount;
    private Long waterCount;
    private Integer lastCareGap;
    private Long weekCareCount;
    private Long wateringOutsideRecommendedCount;
    private Boolean wateringTimeSuggested;
    private String wateringTimeTip;
    private List<WateringTimeSegmentCount> wateringTimeDistribution = new ArrayList<>();
    private List<PlantStatusTimeDistributionItem> statusTimeDistribution = new ArrayList<>();
}
