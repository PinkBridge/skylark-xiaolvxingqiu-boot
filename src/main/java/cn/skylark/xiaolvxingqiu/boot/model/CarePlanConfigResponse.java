package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CarePlanConfigResponse {
    private Long plantId;
    private Boolean enabled;
    private Boolean seasonalMode;
    private List<CarePlanRuleConfig> rules = new ArrayList<>();
}
