package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CarePlanRuleEntity {
    private Long id;
    private Long userId;
    private Long planId;
    private String activityType;
    private String season;
    private Boolean enabled;
    private Integer intervalDays;
    private LocalDate nextDueDate;
}
