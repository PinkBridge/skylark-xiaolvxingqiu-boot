package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class UserCoinAccount {
    private Long userId;
    private Long coinBalance;
    private Long completedActivityTotal;
    private Integer progressActivityCount;
}
