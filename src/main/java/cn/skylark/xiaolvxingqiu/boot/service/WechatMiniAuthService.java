package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.model.UserProfile;
import cn.skylark.xiaolvxingqiu.boot.model.WechatPhoneAuthRequest;
import cn.skylark.xiaolvxingqiu.boot.model.WechatPhoneAuthResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class WechatMiniAuthService {

    private final ObjectMapper objectMapper;
    private final UserProfileService userProfileService;
    private final RestTemplate restTemplate;

    @Value("${app.wechat.app-id:}")
    private String appId;

    @Value("${app.wechat.app-secret:}")
    private String appSecret;

    public WechatMiniAuthService(ObjectMapper objectMapper, UserProfileService userProfileService) {
        this.objectMapper = objectMapper;
        this.userProfileService = userProfileService;
        this.restTemplate = new RestTemplate();
    }

    public WechatPhoneAuthResponse authPhoneAndSaveProfile(Long userId, WechatPhoneAuthRequest request) {
        String sessionKey = fetchSessionKey(request.getCode());
        String phone = decryptPhone(request.getEncryptedData(), request.getIv(), sessionKey);
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("failed to resolve phone number from wechat response");
        }

        UserProfile profile = userProfileService.mergeWechatProfile(
                userId,
                null,
                null,
                null,
                phone.trim()
        );

        WechatPhoneAuthResponse response = new WechatPhoneAuthResponse();
        response.setPhone(profile.getPhone());
        response.setAvatar(profile.getAvatar());
        response.setName(profile.getName());
        response.setGender(profile.getGender());
        return response;
    }

    private String fetchSessionKey(String code) {
        if (appId == null || appId.trim().isEmpty() || appSecret == null || appSecret.trim().isEmpty()) {
            throw new IllegalStateException("wechat appId/appSecret not configured");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, appId, appSecret, code);
        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("empty response from wechat jscode2session");
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            String sessionKey = node.path("session_key").asText("");
            if (sessionKey.isEmpty()) {
                String errMsg = node.path("errmsg").asText("unknown wechat error");
                throw new IllegalArgumentException("wechat jscode2session failed: " + errMsg);
            }
            return sessionKey;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            throw new IllegalArgumentException("invalid response from wechat jscode2session");
        }
    }

    private String decryptPhone(String encryptedData, String iv, String sessionKey) {
        try {
            byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] result = cipher.doFinal(dataBytes);
            String plain = new String(result, StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(plain);
            String phone = node.path("phoneNumber").asText("");
            if (phone.isEmpty()) {
                phone = node.path("purePhoneNumber").asText("");
            }
            return phone;
        } catch (Exception e) {
            throw new IllegalArgumentException("wechat phone decrypt failed");
        }
    }
}
