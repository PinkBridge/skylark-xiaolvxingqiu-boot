package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.UserSubscribeSettingMapper;
import cn.skylark.xiaolvxingqiu.boot.model.SubscribeSettingRequest;
import cn.skylark.xiaolvxingqiu.boot.model.SubscribeSettingResponse;
import cn.skylark.xiaolvxingqiu.boot.model.UserSubscribeSetting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SubscribeSettingService {

    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final String DEFAULT_PUSH_TIME = "09:00";

    private final UserSubscribeSettingMapper userSubscribeSettingMapper;

    @Value("${app.wechat.subscribe.template-id:}")
    private String subscribeTemplateId;

    public SubscribeSettingService(UserSubscribeSettingMapper userSubscribeSettingMapper) {
        this.userSubscribeSettingMapper = userSubscribeSettingMapper;
    }

    public SubscribeSettingResponse getByUserId(Long userId) {
        UserSubscribeSetting setting = userSubscribeSettingMapper.selectByUserId(userId);
        return toResponse(setting);
    }

    public SubscribeSettingResponse save(Long userId, SubscribeSettingRequest request) {
        UserSubscribeSetting current = userSubscribeSettingMapper.selectByUserId(userId);
        UserSubscribeSetting setting = current == null ? new UserSubscribeSetting() : current;
        setting.setUserId(userId);
        setting.setTemplateId(normalizeTemplateId());
        setting.setPushTime(normalizePushTime(request == null ? null : request.getPushTime()));
        setting.setEnabled(request != null && Boolean.TRUE.equals(request.getEnabled()));

        String authStatus = normalizeAuthStatus(request == null ? null : request.getAuthStatus());
        if (!authStatus.isEmpty()) {
            setting.setAuthStatus(authStatus);
            setting.setLastAuthAt(LocalDateTime.now());
        } else if (setting.getAuthStatus() == null || setting.getAuthStatus().trim().isEmpty()) {
            setting.setAuthStatus("UNKNOWN");
        }

        userSubscribeSettingMapper.upsert(setting);
        return getByUserId(userId);
    }

    public List<UserSubscribeSetting> listDueUsers(String pushTime, LocalDate today) {
        return userSubscribeSettingMapper.selectDueUsersByTime(pushTime, today);
    }

    public void markDispatchResult(Long userId, LocalDate pushDate, boolean success, String result) {
        String safeResult = result == null ? "" : result.trim();
        if (safeResult.length() > 250) {
            safeResult = safeResult.substring(0, 250);
        }
        userSubscribeSettingMapper.updateDispatchResult(
                userId,
                pushDate,
                success ? "SUCCESS" : "FAILED",
                safeResult
        );
    }

    private SubscribeSettingResponse toResponse(UserSubscribeSetting setting) {
        SubscribeSettingResponse response = new SubscribeSettingResponse();
        response.setTemplateId(normalizeTemplateId());
        if (setting == null) {
            response.setEnabled(false);
            response.setPushTime(DEFAULT_PUSH_TIME);
            response.setAuthStatus("UNKNOWN");
            return response;
        }
        response.setEnabled(Boolean.TRUE.equals(setting.getEnabled()));
        response.setPushTime(normalizePushTime(setting.getPushTime()));
        response.setAuthStatus(normalizeAuthStatus(setting.getAuthStatus()).isEmpty()
                ? "UNKNOWN"
                : normalizeAuthStatus(setting.getAuthStatus()));
        return response;
    }

    private String normalizeTemplateId() {
        return subscribeTemplateId == null ? "" : subscribeTemplateId.trim();
    }

    private String normalizePushTime(String pushTime) {
        String raw = pushTime == null ? "" : pushTime.trim();
        if (TIME_PATTERN.matcher(raw).matches()) {
            return raw;
        }
        return DEFAULT_PUSH_TIME;
    }

    private String normalizeAuthStatus(String authStatus) {
        String raw = authStatus == null ? "" : authStatus.trim().toUpperCase();
        if (raw.isEmpty()) return "";
        if ("ACCEPT".equals(raw) || "REJECT".equals(raw) || "BAN".equals(raw) || "UNKNOWN".equals(raw)) {
            return raw;
        }
        return "UNKNOWN";
    }
}
