"""
服务端编排最小示例：DAG 调度 + 并行 + 超时 + 重试退避 + 并发限制 + 耗时统计。

只用标准库 asyncio，不需要装任何东西。直接运行：
    python orchestrator.py

对应文档：BE/08-服务端编排与任务流.md
"""

import asyncio
import random
import time
from dataclasses import dataclass, field
from typing import Awaitable, Callable


# 一个节点 = 一个任务：声明它依赖哪些节点，以及怎么执行。
@dataclass
class Node:
    name: str
    deps: list[str]
    # run 接收 ctx(已完成节点的结果字典)，返回这个节点的结果
    run: Callable[[dict], Awaitable[str]]
    timeout: float = 2.0


# 全局并发限制：同一时刻最多 3 个节点在真正执行，避免一次性打爆下游。
SEM = asyncio.Semaphore(3)


async def with_retry(fn: Callable[[], Awaitable[str]], attempts: int = 3) -> str:
    """临时失败时重试，指数退避：200ms, 400ms, 800ms。只对可安全重试的操作用。"""
    for i in range(attempts):
        try:
            return await fn()
        except Exception:
            if i == attempts - 1:
                raise
            # 退避等待，逐次翻倍，避免一拥而上压垮下游
            await asyncio.sleep(0.2 * 2 ** i)
    raise RuntimeError("unreachable")


async def run_node(node: Node, ctx: dict) -> str:
    """执行单个节点，包好：并发限制 + 超时 + 重试 + 耗时统计。"""
    async with SEM:  # 拿不到名额就排队，限制全局并发
        start = time.monotonic()
        try:
            # asyncio.wait_for 给单次调用设超时；with_retry 负责失败重试
            result = await with_retry(
                lambda: asyncio.wait_for(node.run(ctx), timeout=node.timeout)
            )
            return result
        finally:
            elapsed = (time.monotonic() - start) * 1000
            print(f"  [节点完成] {node.name:10s} 耗时 {elapsed:6.0f}ms")


async def run_workflow(nodes: dict[str, Node]) -> dict:
    """
    DAG 调度核心：反复找出“依赖都已完成”的节点，并发执行它们，
    直到所有节点完成。无依赖关系的节点会在同一批里并行跑。
    """
    done: dict[str, str] = {}
    pending = set(nodes)
    total_start = time.monotonic()

    while pending:
        ready = [n for n in pending if all(d in done for d in nodes[n].deps)]
        if not ready:
            raise RuntimeError("检测到循环依赖或缺失依赖")
        print(f"[本批并行执行] {ready}")
        results = await asyncio.gather(*(run_node(nodes[n], done) for n in ready))
        for name, res in zip(ready, results):
            done[name] = res
            pending.discard(name)

    total = (time.monotonic() - total_start) * 1000
    print(f"[全部完成] 总耗时 {total:.0f}ms")
    return done


# ---- 下面是模拟的下游 AI 调用：随机耗时，并以一定概率失败（演示重试）----

async def fake_call(label: str, base_ms: int, fail_rate: float = 0.0) -> str:
    await asyncio.sleep(base_ms / 1000 + random.random() * 0.1)
    if random.random() < fail_rate:
        raise RuntimeError(f"{label} 临时失败")
    return f"{label}-result"


async def main():
    # 工作流：matte 和 bg 无依赖可并行；compose 依赖两者；upscale 依赖 compose。
    nodes = {
        "matte":   Node("matte",   [],                 lambda c: fake_call("抠图", 300)),
        "bg":      Node("bg",      [],                 lambda c: fake_call("生成背景", 500, fail_rate=0.5)),
        "compose": Node("compose", ["matte", "bg"],    lambda c: fake_call("合成", 200)),
        "upscale": Node("upscale", ["compose"],        lambda c: fake_call("超分", 400)),
    }
    result = await run_workflow(nodes)
    print("结果：", result)


if __name__ == "__main__":
    asyncio.run(main())
