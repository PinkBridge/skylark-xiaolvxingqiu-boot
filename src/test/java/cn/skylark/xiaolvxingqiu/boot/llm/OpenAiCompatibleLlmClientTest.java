package cn.skylark.xiaolvxingqiu.boot.llm;

import cn.skylark.xiaolvxingqiu.boot.config.CsLlmProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class OpenAiCompatibleLlmClientTest {

    @Test
    void completeShouldParseDashScopeCompatibleJson() {
        CsLlmProperties p = new CsLlmProperties();
        p.setEnabled(true);
        p.setBaseUrl("http://127.0.0.1:9999/compatible-mode/v1");
        p.setApiKey("sk-test");
        p.setModel("qwen3.5-plus");

        RestTemplate rt = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rt).build();

        String json = "{\"choices\":[{\"message\":{\"content\":\"  模拟千问回复  \"}}]}";
        server.expect(requestTo("http://127.0.0.1:9999/compatible-mode/v1/chat/completions"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(p, rt);
        Optional<String> out = client.complete("绿萝怎么浇水", "plant_care");

        Assertions.assertTrue(out.isPresent());
        Assertions.assertEquals("模拟千问回复", out.get());
        server.verify();
    }

    @Test
    void whenDisabledShouldReturnEmpty() {
        CsLlmProperties p = new CsLlmProperties();
        p.setEnabled(false);
        p.setApiKey("sk-test");
        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(p, new RestTemplate());
        Assertions.assertFalse(client.complete("hi", "unknown").isPresent());
    }
}
