# Intent 识别测试样例集（Day3）

用于验证 `CsChatService` 的意图识别逻辑（`plant_care/customer_service/unknown`）。

## 样例（20 条）

| # | text | expected_intent |
|---|---|---|
| 1 | 绿萝怎么浇水比较好 | plant_care |
| 2 | 龟背竹需要什么光照 | plant_care |
| 3 | 叶子黄叶了怎么处理 | plant_care |
| 4 | 吊兰多久施肥一次 | plant_care |
| 5 | 土壤太湿会不会烂根 | plant_care |
| 6 | 需要修剪吗，怎么修剪 | plant_care |
| 7 | 我的植物有病虫害怎么办 | plant_care |
| 8 | 请给我一个养护建议 | plant_care |
| 9 | 识别失败了总是报错 | customer_service |
| 10 | 上传失败是什么原因 | customer_service |
| 11 | 登录不了账号怎么办 | customer_service |
| 12 | 为什么订阅提醒收不到 | customer_service |
| 13 | 我的积分怎么没到账 | customer_service |
| 14 | 星币为什么没有增加 | customer_service |
| 15 | 我要找客服人工处理 | customer_service |
| 16 | 反馈入口在哪里 | customer_service |
| 17 | 你好 | unknown |
| 18 | 今天天气不错 | unknown |
| 19 | 帮我看看这个问题 | unknown |
| 20 | 在吗 | unknown |

## 验收建议

- Top1 目标：`>= 85%`（规则版）
- `unknown` 样本应触发 `handoffRecommended=true`
- 若错判集中在某类，优先补权重词表（而不是先改阈值）
