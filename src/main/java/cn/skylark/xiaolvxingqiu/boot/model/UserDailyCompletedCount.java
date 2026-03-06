package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class UserDailyCompletedCount {
    private Long userId;
    private Long completedCount;
}
