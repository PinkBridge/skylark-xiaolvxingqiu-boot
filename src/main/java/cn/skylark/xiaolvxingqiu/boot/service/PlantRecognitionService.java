package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.model.PlantRecognitionItem;
import cn.skylark.xiaolvxingqiu.boot.model.PlantRecognitionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class PlantRecognitionService {

    private static final long ACCESS_TOKEN_SKEW_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final int DEFAULT_TOP_NUM = 1;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.baidu.ai.api-key:}")
    private String apiKey;

    @Value("${app.baidu.ai.secret-key:}")
    private String secretKey;

    @Value("${app.baidu.ai.top-num:1}")
    private Integer topNum;

    @Value("${app.baidu.ai.baike-num:0}")
    private Integer baikeNum;

    private volatile String cachedAccessToken = "";
    private volatile long accessTokenExpiresAt = 0L;

    public PlantRecognitionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public PlantRecognitionResponse recognize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传需要识别的植物图片");
        }
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length == 0) {
                throw new IllegalArgumentException("上传图片内容为空");
            }
            String imageBase64 = Base64.getEncoder().encodeToString(bytes);
            String token = getAccessToken();
            String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant?access_token=" + token;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("image", imageBase64);
            formData.add("top_num", String.valueOf(safeTopNum()));
            formData.add("baike_num", String.valueOf(safeBaikeNum()));

            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(formData, headers), String.class);
            String body = response.getBody();
            if (body == null || body.trim().isEmpty()) {
                throw new IllegalArgumentException("植物识别服务返回为空");
            }
            return parseRecognizeResponse(body);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("植物识别失败，请稍后再试");
        }
    }

    private PlantRecognitionResponse parseRecognizeResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (root.has("error_code")) {
            String errorMsg = root.path("error_msg").asText("unknown");
            throw new IllegalArgumentException("植物识别服务错误: " + errorMsg);
        }
        JsonNode result = root.path("result");
        if (!result.isArray() || result.size() == 0) {
            throw new IllegalArgumentException("未识别到植物信息，请更换清晰图片后重试");
        }

        PlantRecognitionResponse response = new PlantRecognitionResponse();
        for (int i = 0; i < result.size(); i++) {
            JsonNode itemNode = result.get(i);
            JsonNode baikeInfoNode = itemNode.path("baike_info");
            PlantRecognitionItem item = new PlantRecognitionItem();
            item.setName(itemNode.path("name").asText(""));
            item.setScore(itemNode.path("score").asDouble(0D));
            item.setDescription(baikeInfoNode.path("description").asText(""));
            item.setImageUrl(baikeInfoNode.path("image_url").asText(""));
            response.getCandidates().add(item);
            if (i == 0) {
                response.setName(item.getName());
                response.setScore(item.getScore());
                response.setDescription(item.getDescription());
                response.setImageUrl(item.getImageUrl());
            }
        }
        return response;
    }

    private String getAccessToken() {
        long now = System.currentTimeMillis();
        if (!cachedAccessToken.isEmpty() && now < accessTokenExpiresAt) {
            return cachedAccessToken;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (!cachedAccessToken.isEmpty() && now < accessTokenExpiresAt) {
                return cachedAccessToken;
            }
            if (apiKey == null || apiKey.trim().isEmpty() || secretKey == null || secretKey.trim().isEmpty()) {
                throw new IllegalStateException("百度AI apiKey/secretKey 未配置");
            }
            String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id={apiKey}&client_secret={secretKey}";
            ResponseEntity<String> tokenResp = restTemplate.getForEntity(url, String.class, apiKey, secretKey);
            String body = tokenResp.getBody();
            if (body == null || body.trim().isEmpty()) {
                throw new IllegalArgumentException("百度AI token 响应为空");
            }
            try {
                JsonNode node = objectMapper.readTree(body);
                String token = node.path("access_token").asText("");
                long expiresInSec = node.path("expires_in").asLong(0L);
                if (token.isEmpty() || expiresInSec <= 0) {
                    String errorMsg = node.path("error_description").asText(node.path("error").asText("unknown"));
                    throw new IllegalArgumentException("获取百度AI token失败: " + errorMsg);
                }
                cachedAccessToken = token;
                accessTokenExpiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresInSec) - ACCESS_TOKEN_SKEW_MILLIS;
                return token;
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("解析百度AI token失败");
            }
        }
    }

    private int safeTopNum() {
        if (topNum == null || topNum <= 0) return DEFAULT_TOP_NUM;
        return Math.min(topNum, 3);
    }

    private int safeBaikeNum() {
        if (baikeNum == null) return 0;
        return baikeNum > 0 ? 1 : 0;
    }
}
