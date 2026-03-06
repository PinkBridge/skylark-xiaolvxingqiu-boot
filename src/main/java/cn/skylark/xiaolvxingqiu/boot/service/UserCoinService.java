package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.CareActivityMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.UserCoinMapper;
import cn.skylark.xiaolvxingqiu.boot.model.UserCoinAccount;
import cn.skylark.xiaolvxingqiu.boot.model.UserCoinAccountSummary;
import cn.skylark.xiaolvxingqiu.boot.model.UserDailyCompletedCount;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserCoinService {

    private static final String COIN_REASON_DAILY_SETTLE = "CARE_ACTIVITY_DAILY_SETTLE";
    private static final int EXCHANGE_RATE = 20;

    private final UserCoinMapper userCoinMapper;
    private final CareActivityMapper careActivityMapper;
    private final ObjectMapper objectMapper;

    public UserCoinService(UserCoinMapper userCoinMapper, CareActivityMapper careActivityMapper, ObjectMapper objectMapper) {
        this.userCoinMapper = userCoinMapper;
        this.careActivityMapper = careActivityMapper;
        this.objectMapper = objectMapper;
    }

    public UserCoinAccountSummary getAccountSummary(Long userId) {
        userCoinMapper.insertAccountIfAbsent(userId);
        UserCoinAccount account = userCoinMapper.selectAccountByUserId(userId);
        if (account == null) {
            account = new UserCoinAccount();
            account.setUserId(userId);
            account.setCoinBalance(0L);
            account.setCompletedActivityTotal(0L);
            account.setProgressActivityCount(0);
        }
        UserCoinAccountSummary summary = new UserCoinAccountSummary();
        int progress = account.getProgressActivityCount() == null ? 0 : Math.max(0, account.getProgressActivityCount());
        summary.setCoinBalance(account.getCoinBalance() == null ? 0L : account.getCoinBalance());
        summary.setProgressActivityCount(progress);
        summary.setNextCoinNeed(progress == 0 ? EXCHANGE_RATE : EXCHANGE_RATE - progress);
        summary.setCompletedActivityTotal(account.getCompletedActivityTotal() == null ? 0L : account.getCompletedActivityTotal());
        return summary;
    }

    @Transactional
    public int settleByDate(LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        List<UserDailyCompletedCount> rows = careActivityMapper.countCompletedByUserOnDate(targetDate);
        int settledUsers = 0;
        for (UserDailyCompletedCount row : rows) {
            if (row == null || row.getUserId() == null) continue;
            long completed = row.getCompletedCount() == null ? 0L : row.getCompletedCount();
            if (completed <= 0) continue;
            boolean changed = settleOneUser(targetDate, row.getUserId(), completed);
            if (changed) settledUsers += 1;
        }
        return settledUsers;
    }

    private boolean settleOneUser(LocalDate targetDate, Long userId, long completedCount) {
        userCoinMapper.insertAccountIfAbsent(userId);
        UserCoinAccount account = userCoinMapper.selectAccountByUserId(userId);
        if (account == null) return false;

        int oldProgress = account.getProgressActivityCount() == null ? 0 : Math.max(0, account.getProgressActivityCount());
        long oldBalance = account.getCoinBalance() == null ? 0L : account.getCoinBalance();
        long oldCompletedTotal = account.getCompletedActivityTotal() == null ? 0L : account.getCompletedActivityTotal();

        long totalProgress = oldProgress + completedCount;
        long gainCoins = totalProgress / EXCHANGE_RATE;
        int remain = (int) (totalProgress % EXCHANGE_RATE);

        Map<String, Object> meta = new HashMap<>();
        meta.put("completedCount", completedCount);
        meta.put("oldProgress", oldProgress);
        meta.put("newProgress", remain);
        meta.put("gainCoins", gainCoins);
        String metaJson = toJson(meta);
        int inserted = userCoinMapper.insertTxnIgnoreDuplicate(userId, gainCoins, COIN_REASON_DAILY_SETTLE, targetDate.toString(), metaJson);
        if (inserted <= 0) {
            return false;
        }

        account.setCoinBalance(oldBalance + gainCoins);
        account.setProgressActivityCount(remain);
        account.setCompletedActivityTotal(oldCompletedTotal + completedCount);
        userCoinMapper.updateAccount(account);
        return true;
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ignore) {
            return "{}";
        }
    }
}
