"""
集群里的“应用实例”：一个极简服务，演示两件事
1) 自己是哪个实例（INSTANCE_NAME），用来观察负载均衡把请求分给了谁；
2) 把计数器放在共享的 Redis 里，证明状态外置后，请求落到任意实例计数都连续。

对应文档：BE/14-集群负载均衡与网关.md
"""

import os
import redis.asyncio as redis
from fastapi import FastAPI

app = FastAPI()

# 实例名由 docker-compose 通过环境变量注入，三个实例各不相同
INSTANCE = os.environ.get("INSTANCE_NAME", "unknown")
# 所有实例连同一个 Redis —— 这就是“共享状态”，无状态实例的状态都放这里
r = redis.from_url(os.environ.get("REDIS_URL", "redis://redis:6379"))


@app.get("/api/hit")
async def hit():
    # 原子自增一个共享计数器：无论请求被分到哪个实例，都在同一个 key 上累加
    count = await r.incr("global:hits")
    return {"servedBy": INSTANCE, "globalHits": count}


@app.get("/health")
async def health():
    # 负载均衡/网关用它判断这个实例是否健康
    return {"status": "ok", "instance": INSTANCE}
