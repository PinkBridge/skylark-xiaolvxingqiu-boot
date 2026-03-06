package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class UserCoinAccountSummary {
    private Long coinBalance;
    private Integer progressActivityCount;
    private Integer nextCoinNeed;
    private Long completedActivityTotal;
}
