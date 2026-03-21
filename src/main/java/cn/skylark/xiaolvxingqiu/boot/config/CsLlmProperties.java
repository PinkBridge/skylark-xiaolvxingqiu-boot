package cn.skylark.xiaolvxingqiu.boot.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 通义千问（阿里云 DashScope）OpenAI 兼容模式配置。
 * 文档：https://help.aliyun.com/zh/model-studio/developer-reference/use-qwen-by-calling-api
 * 密钥请用环境变量：CS_LLM_API_KEY（勿提交仓库）
 */
@Data
@ConfigurationProperties(prefix = "cs.llm")
public class CsLlmProperties {

    /**
     * 默认关闭，行为与 Week1 纯规则一致。
     */
    private boolean enabled = false;

    /**
     * 兼容模式根路径（不含 /chat/completions），例如：
     * https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * DashScope API-Key（Bearer）。也可不设此项，仅依赖环境变量 CS_LLM_API_KEY。
     * getApiKey() 在配置为空时会再读一次进程环境变量，避免 IDE/终端未继承用户级环境变量时占位符为空。
     */
    @Getter(AccessLevel.NONE)
    private String apiKey = "";

    public String getApiKey() {
        if (StringUtils.hasText(this.apiKey)) {
            return this.apiKey.trim();
        }
        String env = System.getenv("CS_LLM_API_KEY");
        return StringUtils.hasText(env) ? env.trim() : "";
    }

    /**
     * 模型名以百炼控制台为准，例如 qwen3.5-plus、qwen-plus、qwen-turbo。
     */
    private String model = "qwen3.5-plus";

    /**
     * 建立 TCP 连接超时（毫秒）。
     */
    private int connectTimeoutMs = 15000;

    /**
     * 等待千问返回完整响应的超时（毫秒）。生成较慢时易超过 30s，默认 120s。
     */
    private int readTimeoutMs = 120000;

    private int maxTokens = 512;
}
