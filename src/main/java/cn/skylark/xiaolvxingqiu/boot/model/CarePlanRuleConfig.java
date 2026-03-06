package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class CarePlanRuleConfig {
    @NotBlank(message = "activityType cannot be empty")
    private String activityType;
    @NotBlank(message = "season cannot be empty")
    private String season;
    private Boolean enabled;
    @Min(value = 1, message = "intervalDays must >= 1")
    private Integer intervalDays;
}
