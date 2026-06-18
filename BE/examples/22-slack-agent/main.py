"""
Slack app 风格 Agent 后端骨架：3 秒内 ACK + 后台异步处理 + 事件幂等去重 + 验签。

对应文档：BE/22-实战C-ai-agent后端.md

为了能独立跑，外部依赖都用本地假实现：
- 幂等去重用进程内 set（生产用 Redis SET NX）；
- “调 AI / 回贴 Slack” 用打印模拟。

运行：
    pip install -r requirements.txt
    uvicorn main:app --reload

模拟一次事件回调：
    curl -X POST localhost:8000/slack/events -H 'Content-Type: application/json' \
      -d '{"type":"event_callback","event_id":"Ev123","event":{"type":"app_mention","text":"hi","channel":"C1"}}'
重复发同一个 event_id，会被幂等跳过（worker 不再处理第二次）。
"""

import asyncio
import hashlib
import hmac
import os
import time

from fastapi import BackgroundTasks, FastAPI, Request

app = FastAPI(title="Slack Agent (demo)")

# 进程内幂等记录（生产用 Redis：SET key NX EX）
SEEN_EVENTS: set[str] = set()
SIGNING_SECRET = os.environ.get("SLACK_SIGNING_SECRET", "demo-secret")


def verify_slack_signature(body: bytes, timestamp: str, signature: str) -> bool:
    """
    校验请求确实来自 Slack（验签）。Slack 用 signing secret 对
    "v0:{timestamp}:{body}" 做 HMAC-SHA256，我们重算并比对。
    演示里若没带签名头则跳过（方便用 curl 测）；生产必须强制校验。
    """
    if not signature:
        return True  # demo-only：放过没签名的本地请求
    try:
        ts = int(timestamp)
    except ValueError:
        return False
    # 防重放：真实 Slack 请求时间戳不应和当前时间差太远。
    if abs(time.time() - ts) > 60 * 5:
        return False
    base = f"v0:{timestamp}:{body.decode()}".encode()
    expected = "v0=" + hmac.new(SIGNING_SECRET.encode(), base, hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, signature)


async def call_ai_and_reply(event: dict):
    """后台干活：调 AI/工具，再调 Slack API 回贴。这里用打印模拟。"""
    await asyncio.sleep(1.0)  # 模拟 AI 处理耗时（正因为它慢，才必须异步）
    reply = f"[AI 回复] 收到你的消息：{event.get('text', '')}"
    # 真实场景：用该团队的 bot token 调 chat.postMessage 回贴到 event['channel']
    print(f"-> 回贴到频道 {event.get('channel')}: {reply}")


async def handle_event(body: dict):
    event_id = body.get("event_id", "")
    # 幂等：Slack 可能重复投递，见过就跳过，避免重复回复
    if event_id in SEEN_EVENTS:
        print(f"[幂等] 事件 {event_id} 已处理过，跳过")
        return
    SEEN_EVENTS.add(event_id)
    await call_ai_and_reply(body.get("event", {}))


@app.post("/slack/events")
async def slack_events(req: Request, background: BackgroundTasks):
    raw = await req.body()
    # 验签（入站方向的鉴权）
    if not verify_slack_signature(
        raw,
        req.headers.get("X-Slack-Request-Timestamp", str(int(time.time()))),
        req.headers.get("X-Slack-Signature", ""),
    ):
        return {"ok": False, "error": "bad signature"}

    body = await req.json()

    # Slack 首次配置回调地址时会发 url_verification，要原样回 challenge
    if body.get("type") == "url_verification":
        return {"challenge": body["challenge"]}

    # 关键：不在这里等 AI（会超过 Slack 3 秒限制）。丢后台、立刻 ACK。
    # 生产用消息队列 + worker 替代 BackgroundTasks（可靠、可扩容、跨实例）。
    background.add_task(handle_event, body)
    return {"ok": True}
