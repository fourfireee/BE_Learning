# 22 · Slack Agent 后端骨架

- 对应文档：`BE/22-实战C-ai-agent后端.md`
- 演示外部集成的两条铁律：3 秒内 ACK（长任务异步） + 事件幂等去重；外加入站验签。
- 外部依赖（Redis、AI、Slack API）都用本地假实现，可独立跑。

## 运行

```bash
pip install -r requirements.txt
uvicorn main:app --reload
```

## 试一试

```bash
# 模拟一次事件回调：接口立刻返回 {"ok":true}（ACK），AI 处理在后台进行
curl -X POST localhost:8000/slack/events -H 'Content-Type: application/json' \
  -d '{"type":"event_callback","event_id":"Ev123","event":{"type":"app_mention","text":"帮我总结","channel":"C1"}}'
```

- 观察 uvicorn 控制台：约 1 秒后打印 `-> 回贴到频道 C1: [AI 回复] ...`，说明后台异步处理完成了。
- 接口本身是立刻返回的（满足 Slack 3 秒 ACK），不等 AI。

```bash
# 重复发同一个 event_id：第二次会被幂等跳过（控制台打印 [幂等] ... 跳过）
curl -X POST localhost:8000/slack/events -H 'Content-Type: application/json' \
  -d '{"type":"event_callback","event_id":"Ev123","event":{"text":"again","channel":"C1"}}'
```

```bash
# url_verification：Slack 配置回调地址时的握手，原样回 challenge
curl -X POST localhost:8000/slack/events -H 'Content-Type: application/json' \
  -d '{"type":"url_verification","challenge":"abc123"}'
# => {"challenge":"abc123"}
```

## 接入真实 Slack 要补的

- 验签：生产强制校验 `X-Slack-Signature`（本示例没带签名头时放行，仅为方便本地测）。
- 异步：把 `BackgroundTasks` 换成消息队列 + worker（重启不丢、可扩容、跨实例）。
- 幂等：把进程内 `SEEN_EVENTS` 换成 Redis `SET event_id NX EX 3600`。
- OAuth2：走安装流程拿各团队的 bot token，存库；回贴时用对应 token 调 `chat.postMessage`。
- 公网：Slack 回调地址必须是公网 HTTPS（本地调试可用 ngrok 之类内网穿透）。
