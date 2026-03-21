package cn.skylark.xiaolvxingqiu.boot.llm;

import java.util.Optional;

/**
 * 大模型对话（当前对接通义千问 DashScope OpenAI 兼容接口）。
 * 未启用或调用失败时返回空，由 {@link cn.skylark.xiaolvxingqiu.boot.service.CsChatService} 回退规则模板。
 */
public interface LlmClient {

    /**
     * @param userMessage 用户原始问题
     * @param intent      规则意图：plant_care / customer_service / unknown
     */
    Optional<String> complete(String userMessage, String intent);

    static LlmClient noop() {
        return (userMessage, intent) -> Optional.empty();
    }
}
