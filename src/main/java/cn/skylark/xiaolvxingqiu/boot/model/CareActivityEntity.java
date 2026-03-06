package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CareActivityEntity {
    private Long id;
    private Long userId;
    private Long plantId;
    private Long planId;
    private Long ruleId;
    private String activityType;
    private LocalDate scheduledDate;
    private String status;
    private LocalDateTime completedAt;
    private String recordJson;
    private String plantName;
}
