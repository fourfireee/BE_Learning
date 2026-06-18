"""
AIGC 编排服务最小示例：提交工作流 -> 后台异步按 DAG 执行 -> SSE 推送进度。

对应文档：BE/21-实战B-aigc编排服务.md

运行：
    pip install -r requirements.txt
    uvicorn main:app --reload

演示：
    # 1) 提交任务，立刻拿到 taskId（202 思路）
    curl -X POST localhost:8000/v1/tasks
    # 2) 用返回的 taskId 订阅 SSE 进度流
    curl -N localhost:8000/v1/tasks/<taskId>/events

注意：示例用进程内字典存任务状态，仅供单机演示。
生产应放 Redis/DB，并把执行交给独立 worker（见文档）。
"""

import asyncio
import json
import time
import uuid

from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse

app = FastAPI(title="AIGC Workflow (demo)")

# 进程内任务状态：taskId -> {status, steps: [...], result}
TASKS: dict[str, dict] = {}


# DAG 定义：(节点名, 依赖, 模拟耗时秒)
WORKFLOW = [
    ("matte",   [],                0.6),  # 抠图
    ("bg",      [],                1.0),  # 生成背景（与抠图并行）
    ("compose", ["matte", "bg"],   0.4),  # 合成（依赖前两个）
    ("upscale", ["compose"],       0.8),  # 超分
]


async def run_node(task_id: str, name: str, cost: float):
    """模拟调用一个下游 AI 服务，并记录单步耗时（编排篇的纪律）。"""
    start = time.monotonic()
    await asyncio.sleep(cost)  # 模拟网络等待
    elapsed_ms = int((time.monotonic() - start) * 1000)
    TASKS[task_id]["steps"].append({"node": name, "elapsedMs": elapsed_ms})


async def run_workflow(task_id: str):
    """DAG 调度：每一批并发执行依赖已满足的节点，直到全部完成。"""
    TASKS[task_id]["status"] = "running"
    done: set[str] = set()
    pending = {n for n, _, _ in WORKFLOW}
    spec = {n: (deps, cost) for n, deps, cost in WORKFLOW}

    try:
        while pending:
            ready = [n for n in pending if all(d in done for d in spec[n][0])]
            # 并发跑这一批（无依赖关系的节点并行）
            await asyncio.gather(*(run_node(task_id, n, spec[n][1]) for n in ready))
            done.update(ready)
            pending -= set(ready)

        TASKS[task_id]["status"] = "succeeded"
        TASKS[task_id]["result"] = {"output": "s3://bucket/result.png"}  # 假装产物地址
    except Exception as e:
        # demo 里节点一般不会失败；这里仍把失败写回状态，避免 SSE 一直挂着。
        TASKS[task_id]["status"] = "failed"
        TASKS[task_id]["result"] = {"error": str(e)}


@app.post("/v1/tasks", status_code=202)
async def submit():
    """提交任务：建状态、后台异步执行、立刻返回 taskId（不让客户端干等）。"""
    task_id = uuid.uuid4().hex[:8]
    TASKS[task_id] = {"status": "pending", "steps": [], "result": None}
    # 后台异步执行；生产里这一步应改为投递到消息队列，由 worker 消费
    asyncio.create_task(run_workflow(task_id))
    return {"taskId": task_id, "status": "pending"}


@app.get("/v1/tasks/{task_id}")
def get_task(task_id: str):
    if task_id not in TASKS:
        raise HTTPException(404, "task not found")
    return {"taskId": task_id, **TASKS[task_id]}


@app.get("/v1/tasks/{task_id}/events")
async def task_events(task_id: str):
    """SSE：持续把任务进度作为事件流推给客户端，直到任务结束。"""
    if task_id not in TASKS:
        raise HTTPException(404, "task not found")

    async def gen():
        last_sent = 0
        while True:
            task = TASKS[task_id]
            # 只推“新完成的步骤”，避免重复
            steps = task["steps"]
            while last_sent < len(steps):
                yield f"event: step\ndata: {json.dumps(steps[last_sent])}\n\n"
                last_sent += 1
            if task["status"] in ("succeeded", "failed"):
                yield f"event: done\ndata: {json.dumps({'status': task['status'], 'result': task['result']})}\n\n"
                return
            await asyncio.sleep(0.2)

    return StreamingResponse(gen(), media_type="text/event-stream")
