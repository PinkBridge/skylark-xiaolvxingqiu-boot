package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.llm.LlmClient;
import cn.skylark.xiaolvxingqiu.boot.model.CsChatRequest;
import cn.skylark.xiaolvxingqiu.boot.model.CsChatResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CsChatServiceIntentTest {

    @Test
    void intentSamplesShouldMatchExpected() {
        CsChatService service = new CsChatService(LlmClient.noop());
        List<SampleCase> cases = buildCases();

        int hit = 0;
        for (SampleCase item : cases) {
            CsChatRequest request = new CsChatRequest();
            request.setSessionId("test-session");
            request.setMessage(item.text);

            CsChatResponse response = service.chat(request);
            String actualIntent = response.getIntent();
            if (item.expectedIntent.equals(actualIntent)) {
                hit++;
            }

            if ("unknown".equals(item.expectedIntent)) {
                Assertions.assertTrue(response.isHandoffRecommended(), "unknown case should recommend handoff: " + item.text);
            }
        }

        double accuracy = (double) hit / (double) cases.size();
        Assertions.assertTrue(accuracy >= 0.85D, "intent accuracy should be >= 0.85, actual=" + accuracy);
    }

    private List<SampleCase> buildCases() {
        List<SampleCase> items = new ArrayList<SampleCase>();
        items.add(new SampleCase("绿萝怎么浇水比较好", "plant_care"));
        items.add(new SampleCase("龟背竹需要什么光照", "plant_care"));
        items.add(new SampleCase("叶子黄叶了怎么处理", "plant_care"));
        items.add(new SampleCase("吊兰多久施肥一次", "plant_care"));
        items.add(new SampleCase("土壤太湿会不会烂根", "plant_care"));
        items.add(new SampleCase("需要修剪吗，怎么修剪", "plant_care"));
        items.add(new SampleCase("我的植物有病虫害怎么办", "plant_care"));
        items.add(new SampleCase("请给我一个养护建议", "plant_care"));

        items.add(new SampleCase("识别失败了总是报错", "customer_service"));
        items.add(new SampleCase("上传失败是什么原因", "customer_service"));
        items.add(new SampleCase("登录不了账号怎么办", "customer_service"));
        items.add(new SampleCase("为什么订阅提醒收不到", "customer_service"));
        items.add(new SampleCase("我的积分怎么没到账", "customer_service"));
        items.add(new SampleCase("星币为什么没有增加", "customer_service"));
        items.add(new SampleCase("我要找客服人工处理", "customer_service"));
        items.add(new SampleCase("反馈入口在哪里", "customer_service"));

        items.add(new SampleCase("你好", "unknown"));
        items.add(new SampleCase("今天天气不错", "unknown"));
        items.add(new SampleCase("帮我看看这个问题", "unknown"));
        items.add(new SampleCase("在吗", "unknown"));
        return items;
    }

    private static class SampleCase {
        private final String text;
        private final String expectedIntent;

        private SampleCase(String text, String expectedIntent) {
            this.text = text;
            this.expectedIntent = expectedIntent;
        }
    }
}
