package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.CareActivityMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.UserWechatIdentityMapper;
import cn.skylark.xiaolvxingqiu.boot.model.UserSubscribeSetting;
import cn.skylark.xiaolvxingqiu.boot.model.UserWechatIdentity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WechatSubscribePushService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserWechatIdentityMapper userWechatIdentityMapper;
    private final CareActivityMapper careActivityMapper;
    private final CareMetaService careMetaService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.wechat.app-id:}")
    private String appId;

    @Value("${app.wechat.app-secret:}")
    private String appSecret;

    @Value("${app.wechat.subscribe.template-id:}")
    private String templateId;

    private volatile String cachedAccessToken = "";
    private volatile long cachedAccessTokenExpireAtMs = 0L;

    public WechatSubscribePushService(UserWechatIdentityMapper userWechatIdentityMapper,
                                      CareActivityMapper careActivityMapper,
                                      CareMetaService careMetaService,
                                      ObjectMapper objectMapper) {
        this.userWechatIdentityMapper = userWechatIdentityMapper;
        this.careActivityMapper = careActivityMapper;
        this.careMetaService = careMetaService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public PushResult pushDailyCareReminder(UserSubscribeSetting setting, LocalDate today) {
        if (setting == null || setting.getUserId() == null) {
            return PushResult.skip("invalid setting");
        }
        Long userId = setting.getUserId();
        long pendingCount = careActivityMapper.countPendingByUserOnDate(userId, today);
        long pendingPlantCount = careActivityMapper.countPendingPlantByUserOnDate(userId, today);
        UserWechatIdentity identity = userWechatIdentityMapper.selectByUserId(userId);
        if (identity == null || identity.getOpenid() == null || identity.getOpenid().trim().isEmpty()) {
            return PushResult.fail("openid missing");
        }

        String accessToken = resolveAccessToken();
        String resolvedTemplateId = normalizeText(setting.getTemplateId());
        if (resolvedTemplateId.isEmpty()) {
            resolvedTemplateId = normalizeText(templateId);
        }
        if (resolvedTemplateId.isEmpty()) {
            return PushResult.fail("template id not configured");
        }

        List<String> types = careActivityMapper.selectPendingTypesByUserOnDate(userId, today, 3);
        List<String> labels = new ArrayList<>();
        for (String type : types) {
            String label = normalizeText(careMetaService.label(type));
            if (!label.isEmpty() && !labels.contains(label)) {
                labels.add(label);
            }
        }

        String eventType = labels.isEmpty() ? (pendingCount > 0 ? "养护提醒" : "系统提醒") : String.join("/", labels);
        String gardenName = normalizeText(careActivityMapper.selectFirstPendingGardenName(userId, today));
        String plantName = normalizeText(careActivityMapper.selectFirstPendingPlantName(userId, today));
        String place = !gardenName.isEmpty() ? gardenName : (!plantName.isEmpty() ? plantName : "我的花园");
        String thing = pendingPlantCount > 0
                ? ("你有" + pendingPlantCount + "颗绿植等待养护")
                : "今天没有绿植等待养护";
        String timeValue = DATE_FORMATTER.format(today) + " " + normalizeText(setting.getPushTime());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing3", valueNode(thing, 20));
        data.put("thing7", valueNode(eventType, 20));
        data.put("thing4", valueNode(place, 20));
        data.put("time6", valueNode(timeValue, 20));

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("touser", identity.getOpenid());
        req.put("template_id", resolvedTemplateId);
        req.put("page", "pages/care/care");
        req.put("data", data);

        try {
            String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String body = response.getBody();
            if (body == null || body.trim().isEmpty()) {
                return PushResult.fail("empty subscribe send response");
            }
            JsonNode node = objectMapper.readTree(body);
            int errCode = node.path("errcode").asInt(-1);
            String errMsg = node.path("errmsg").asText("");
            if (errCode == 0) {
                return PushResult.success("ok");
            }
            return PushResult.fail("wechat send failed: " + errCode + " " + errMsg);
        } catch (Exception e) {
            return PushResult.fail("wechat send exception");
        }
    }

    private Map<String, String> valueNode(String value, int maxLen) {
        Map<String, String> node = new LinkedHashMap<>();
        String safe = normalizeText(value);
        if (safe.length() > maxLen) {
            safe = safe.substring(0, maxLen);
        }
        node.put("value", safe);
        return node;
    }

    private String resolveAccessToken() {
        long now = System.currentTimeMillis();
        if (!cachedAccessToken.isEmpty() && now < cachedAccessTokenExpireAtMs) {
            return cachedAccessToken;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (!cachedAccessToken.isEmpty() && now < cachedAccessTokenExpireAtMs) {
                return cachedAccessToken;
            }
            if (normalizeText(appId).isEmpty() || normalizeText(appSecret).isEmpty()) {
                throw new IllegalStateException("wechat appId/appSecret not configured");
            }
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, appId, appSecret);
            String body = response.getBody();
            if (body == null || body.trim().isEmpty()) {
                throw new IllegalStateException("empty wechat token response");
            }
            try {
                JsonNode node = objectMapper.readTree(body);
                String token = node.path("access_token").asText("");
                int expiresIn = node.path("expires_in").asInt(0);
                if (token.isEmpty() || expiresIn <= 0) {
                    throw new IllegalStateException("invalid wechat token response");
                }
                cachedAccessToken = token;
                cachedAccessTokenExpireAtMs = System.currentTimeMillis() + Math.max(60, expiresIn - 120) * 1000L;
                return cachedAccessToken;
            } catch (Exception e) {
                throw new IllegalStateException("parse wechat token failed");
            }
        }
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }

    public static class PushResult {
        private final boolean success;
        private final boolean skipped;
        private final String message;

        private PushResult(boolean success, boolean skipped, String message) {
            this.success = success;
            this.skipped = skipped;
            this.message = message;
        }

        public static PushResult success(String message) {
            return new PushResult(true, false, message);
        }

        public static PushResult skip(String message) {
            return new PushResult(false, true, message);
        }

        public static PushResult fail(String message) {
            return new PushResult(false, false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public String getMessage() {
            return message;
        }
    }
}
