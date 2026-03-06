package cn.skylark.xiaolvxingqiu.boot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyCareGenerateJob {

    private final CareActivityService careActivityService;

    public DailyCareGenerateJob(CareActivityService careActivityService) {
        this.careActivityService = careActivityService;
    }

    @Scheduled(cron = "${app.care.generate-cron:0 10 0 * * ?}")
    public void generateToday() {
        careActivityService.generateActivitiesForAllUsers(LocalDate.now());
    }
}
