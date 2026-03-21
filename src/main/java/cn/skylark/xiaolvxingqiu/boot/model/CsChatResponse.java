package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class CsChatResponse {

    private String sessionId;
    private String reply;
    /**
     * rule：规则模板；llm：通义千问生成（失败时仍为 rule）
     */
    private String replySource;
    private String intent;
    private Double confidence;
    private boolean handoffRecommended;
}
