package cn.skylark.xiaolvxingqiu.boot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyCoinSettleJob {

    private final UserCoinService userCoinService;

    public DailyCoinSettleJob(UserCoinService userCoinService) {
        this.userCoinService = userCoinService;
    }

    @Scheduled(cron = "${app.coin.settle-cron:0 55 23 * * ?}")
    public void settleTodayCompletedActivities() {
        userCoinService.settleByDate(LocalDate.now());
    }
}
