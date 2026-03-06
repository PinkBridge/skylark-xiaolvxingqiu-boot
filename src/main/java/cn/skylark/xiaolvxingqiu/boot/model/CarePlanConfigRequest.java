package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
public class CarePlanConfigRequest {
    private Boolean enabled;
    private Boolean seasonalMode;
    @Valid
    private List<CarePlanRuleConfig> rules = new ArrayList<>();
}
