package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.model.UserSubscribeSetting;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DailyCareSubscribeDispatchJob {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final SubscribeSettingService subscribeSettingService;
    private final WechatSubscribePushService wechatSubscribePushService;

    public DailyCareSubscribeDispatchJob(SubscribeSettingService subscribeSettingService,
                                         WechatSubscribePushService wechatSubscribePushService) {
        this.subscribeSettingService = subscribeSettingService;
        this.wechatSubscribePushService = wechatSubscribePushService;
    }

    @Scheduled(cron = "${app.notify.dispatch-cron:0 * * * * ?}")
    public void dispatch() {
        LocalDate today = LocalDate.now();
        String nowTime = LocalTime.now().format(TIME_FORMATTER);
        List<UserSubscribeSetting> dueUsers = subscribeSettingService.listDueUsers(nowTime, today);
        for (UserSubscribeSetting setting : dueUsers) {
            WechatSubscribePushService.PushResult result = wechatSubscribePushService.pushDailyCareReminder(setting, today);
            // Skip should also be marked, otherwise it will retry every minute and waste resources.
            if (result.isSuccess()) {
                subscribeSettingService.markDispatchResult(setting.getUserId(), today, true, result.getMessage());
                continue;
            }
            if (result.isSkipped()) {
                subscribeSettingService.markDispatchResult(setting.getUserId(), today, true, result.getMessage());
                continue;
            }
            subscribeSettingService.markDispatchResult(setting.getUserId(), today, false, result.getMessage());
        }
    }
}
