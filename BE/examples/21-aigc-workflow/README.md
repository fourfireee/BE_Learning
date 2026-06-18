# 21 · AIGC Workflow 编排服务最小示例

- 对应文档：`BE/21-实战B-aigc编排服务.md`
- 演示：提交任务立刻返回 taskId（202 思路）、后台按 DAG 异步执行、SSE 实时推送进度。

## 运行

```bash
pip install -r requirements.txt
uvicorn main:app --reload
```

## 试一试

```bash
# 1) 提交任务，拿到 taskId
curl -X POST localhost:8000/v1/tasks
# => {"taskId":"a1b2c3d4","status":"pending"}

# 2) 订阅 SSE 进度流（-N 关闭缓冲，能实时看到一条条事件）
curl -N localhost:8000/v1/tasks/a1b2c3d4/events

# 或轮询查状态
curl localhost:8000/v1/tasks/a1b2c3d4
```

- SSE 流里会依次看到每个节点完成的事件（含单步耗时），最后一条 `done` 带最终结果。
- 注意 `matte` 和 `bg` 几乎同时完成（并行），`compose`、`upscale` 依次跟上——这就是 DAG 调度。

## 和文档/其他示例的关系

- DAG 调度、并行、耗时统计的纯逻辑版见 `../08-orchestration`（无需任何框架）。
- 这里把它接到了 HTTP + 异步任务 + SSE 上，更接近真实编排服务。

## 生产差距（示例为了简单省略的）

- 任务状态用了进程内字典：多实例/重启会丢。生产放 Redis/DB。
- 用 `asyncio.create_task` 在 web 进程里执行：生产应投递到消息队列，由独立 worker 执行并可扩容。
- 缺少超时/重试/并发限制：参考 `../08-orchestration` 里的 `with_retry` 和信号量补上。
