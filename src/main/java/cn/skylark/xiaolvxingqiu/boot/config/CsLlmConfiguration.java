package cn.skylark.xiaolvxingqiu.boot.config;

import cn.skylark.xiaolvxingqiu.boot.llm.LlmClient;
import cn.skylark.xiaolvxingqiu.boot.llm.OpenAiCompatibleLlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(CsLlmProperties.class)
public class CsLlmConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CsLlmConfiguration.class);

    @Bean
    @Qualifier("csLlmRestTemplate")
    public RestTemplate csLlmRestTemplate(CsLlmProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    @Bean
    public LlmClient llmClient(CsLlmProperties properties, @Qualifier("csLlmRestTemplate") RestTemplate csLlmRestTemplate) {
        log.info("cs.llm: enabled={}, apiKeyConfigured={}, model={}, baseUrl={}",
                properties.isEnabled(),
                StringUtils.hasText(properties.getApiKey()),
                properties.getModel(),
                properties.getBaseUrl());
        return new OpenAiCompatibleLlmClient(properties, csLlmRestTemplate);
    }
}
