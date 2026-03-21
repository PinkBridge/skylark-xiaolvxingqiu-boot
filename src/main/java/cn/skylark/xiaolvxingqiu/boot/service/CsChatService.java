package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.llm.LlmClient;
import cn.skylark.xiaolvxingqiu.boot.model.CsChatRequest;
import cn.skylark.xiaolvxingqiu.boot.model.CsChatResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CsChatService {

    private final LlmClient llmClient;

    public CsChatService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    private static final String INTENT_PLANT_CARE = "plant_care";
    private static final String INTENT_CUSTOMER_SERVICE = "customer_service";
    private static final String INTENT_UNKNOWN = "unknown";
    private static final int MIN_SCORE = 2;
    private static final int MIN_GAP = 2;
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.60D;

    private static final Map<String, Integer> PLANT_CARE_WEIGHTS = buildPlantCareWeights();
    private static final Map<String, Integer> CUSTOMER_SERVICE_WEIGHTS = buildCustomerServiceWeights();

    public CsChatResponse chat(CsChatRequest request) {
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.isEmpty()) {
            throw new IllegalArgumentException("message cannot be empty");
        }

        IntentDecision decision = detectIntent(message);
        CsChatResponse response = new CsChatResponse();
        response.setSessionId(request.getSessionId());
        response.setIntent(decision.intent);
        String reply = generateReply(decision.intent);
        String replySource = "rule";
        Optional<String> llm = llmClient.complete(message, decision.intent);
        if (llm.isPresent()) {
            reply = llm.get();
            replySource = "llm";
        }
        response.setReply(reply);
        response.setReplySource(replySource);
        response.setConfidence(decision.confidence);
        response.setHandoffRecommended(INTENT_UNKNOWN.equals(decision.intent) || decision.confidence < LOW_CONFIDENCE_THRESHOLD);
        return response;
    }

    private IntentDecision detectIntent(String message) {
        String text = message.toLowerCase(Locale.ROOT);
        int plantScore = scoreByWeights(text, PLANT_CARE_WEIGHTS);
        int serviceScore = scoreByWeights(text, CUSTOMER_SERVICE_WEIGHTS);

        int topScore = Math.max(plantScore, serviceScore);
        int secondScore = Math.min(plantScore, serviceScore);

        if (topScore < MIN_SCORE) {
            return new IntentDecision(INTENT_UNKNOWN, 0.45D);
        }

        if (Math.abs(plantScore - serviceScore) < MIN_GAP) {
            return new IntentDecision(INTENT_UNKNOWN, calcConfidence(topScore, secondScore) * 0.70D);
        }

        String intent = plantScore > serviceScore ? INTENT_PLANT_CARE : INTENT_CUSTOMER_SERVICE;
        return new IntentDecision(intent, calcConfidence(topScore, secondScore));
    }

    private int scoreByWeights(String text, Map<String, Integer> weights) {
        int score = 0;
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            String keyword = entry.getKey();
            Integer weight = entry.getValue();
            if (keyword != null && weight != null && weight > 0 && text.contains(keyword.toLowerCase(Locale.ROOT))) {
                score += weight;
            }
        }
        return score;
    }

    private double calcConfidence(int topScore, int secondScore) {
        double raw = (double) topScore / (double) (topScore + secondScore + 1);
        if (raw < 0.35D) return 0.35D;
        if (raw > 0.95D) return 0.95D;
        return raw;
    }

    private String generateReply(String intent) {
        if (INTENT_PLANT_CARE.equals(intent)) {
            return "这是养护类问题。\n"
                    + "建议你先按这 4 项排查：\n"
                    + "1) 近 7 天浇水频率\n"
                    + "2) 光照时长和强度\n"
                    + "3) 通风情况\n"
                    + "4) 最近施肥记录\n"
                    + "你可以补充植物名称、叶片状态和养护记录，我会给你更精确的处理步骤。";
        }
        if (INTENT_CUSTOMER_SERVICE.equals(intent)) {
            return "这是平台客服类问题。\n"
                    + "请补充这些信息，我会更快定位：\n"
                    + "1) 发生页面和按钮\n"
                    + "2) 报错文案或截图\n"
                    + "3) 发生时间\n"
                    + "4) 复现步骤\n"
                    + "我会按问题链路给你下一步处理建议。";
        }
        return "我先理解到你在咨询问题，但当前信息还不够。\n"
                + "请补充植物名称、症状表现或报错截图，我会继续判断；\n"
                + "如果你希望更快处理，也可以直接转人工客服。";
    }

    private static Map<String, Integer> buildPlantCareWeights() {
        Map<String, Integer> weights = new LinkedHashMap<String, Integer>();
        weights.put("病虫害", 4);
        weights.put("黄叶", 4);
        weights.put("浇水", 3);
        weights.put("光照", 3);
        weights.put("施肥", 3);
        weights.put("土壤", 2);
        weights.put("修剪", 2);
        weights.put("养护", 2);
        weights.put("绿萝", 2);
        weights.put("龟背竹", 2);
        weights.put("吊兰", 2);
        return weights;
    }

    private static Map<String, Integer> buildCustomerServiceWeights() {
        Map<String, Integer> weights = new LinkedHashMap<String, Integer>();
        weights.put("识别失败", 4);
        weights.put("上传失败", 4);
        weights.put("报错", 4);
        weights.put("登录", 3);
        weights.put("账号", 3);
        weights.put("订阅", 3);
        weights.put("积分", 3);
        weights.put("星币", 3);
        weights.put("客服", 2);
        weights.put("反馈", 2);
        return weights;
    }

    private static class IntentDecision {
        private final String intent;
        private final double confidence;

        private IntentDecision(String intent, double confidence) {
            this.intent = intent;
            this.confidence = confidence;
        }
    }
}
