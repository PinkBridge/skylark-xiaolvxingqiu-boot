package cn.skylark.xiaolvxingqiu.boot.llm;

import cn.skylark.xiaolvxingqiu.boot.config.CsLlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * OpenAI 兼容 POST {baseUrl}/chat/completions（通义千问 DashScope 兼容模式）。
 */
public class OpenAiCompatibleLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmClient.class);

    private final CsLlmProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiCompatibleLlmClient(CsLlmProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<String> complete(String userMessage, String intent) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            log.warn("cs.llm.enabled=true but cs.llm.api-key is empty, skip Qwen call");
            return Optional.empty();
        }
        String url = buildChatCompletionsUrl();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey().trim());

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("model", properties.getModel());
            body.put("max_tokens", properties.getMaxTokens());
            body.put("temperature", 0.7D);
            body.put("messages", buildMessages(userMessage, intent));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Qwen http not 2xx or empty body, status={}", response.getStatusCode());
                return Optional.empty();
            }
            return parseAssistantContent(response.getBody());
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            log.warn("Qwen HTTP {}: {}", e.getStatusCode(), abbreviate(body, 900));
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Qwen request failed: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Qwen unexpected error", e);
            return Optional.empty();
        }
    }

    private List<Map<String, String>> buildMessages(String userMessage, String intent) {
        String system = buildSystemPrompt(intent);
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        Map<String, String> sys = new HashMap<String, String>();
        sys.put("role", "system");
        sys.put("content", system);
        messages.add(sys);
        Map<String, String> user = new HashMap<String, String>();
        user.put("role", "user");
        user.put("content", userMessage == null ? "" : userMessage);
        messages.add(user);
        return messages;
    }

    private String buildSystemPrompt(String intent) {
        String label;
        if ("plant_care".equals(intent)) {
            label = "养护类（浇水、光照、施肥、病虫害、黄叶等）";
        } else if ("customer_service".equals(intent)) {
            label = "平台客服类（登录、上传、报错、积分、订阅等）";
        } else {
            label = "意图不明确";
        }
        return "你是「小绿星球」绿植小程序的智能助手。\n"
                + "当前用户问题被规则分类为：" + label + "。\n"
                + "要求：用中文简洁回答，给出可操作步骤；不要编造订单/账号信息；不要给出医疗诊断；不要承诺无法验证的结果。\n"
                + "若信息不足，请列出需要用户补充的关键点。";
    }

    private String buildChatCompletionsUrl() {
        String base = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.toLowerCase(Locale.ROOT).endsWith("/chat/completions")) {
            return base;
        }
        return base + "/chat/completions";
    }

    private static String abbreviate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }

    private Optional<String> parseAssistantContent(String jsonBody) throws Exception {
        JsonNode root = objectMapper.readTree(jsonBody);
        if (root.has("error")) {
            log.warn("Qwen error field in response: {}", abbreviate(root.path("error").toString(), 800));
            return Optional.empty();
        }
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            return Optional.empty();
        }
        String text = choices.get(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(text)) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }
}
