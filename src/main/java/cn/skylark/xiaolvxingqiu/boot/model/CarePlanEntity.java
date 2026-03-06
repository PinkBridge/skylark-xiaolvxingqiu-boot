package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class CarePlanEntity {
    private Long id;
    private Long userId;
    private Long plantId;
    private Boolean enabled;
    private Boolean seasonalMode;
}
