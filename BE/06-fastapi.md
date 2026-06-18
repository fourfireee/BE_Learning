# Python 服务端框架：FastAPI

- 你已有的 AIGC 链路是 Python，所以 Python 后端框架很可能用得上。这里以 FastAPI 为主，对比 Flask/Django。
- FastAPI 的卖点：原生 async、基于类型注解做参数校验和文档、性能好、写起来快。
- 配可运行示例见 `examples/06-fastapi-rest`。

## 和 Spring Boot 对照着看

- 概念是相通的，换个语法而已：

| 概念 | Spring Boot | FastAPI |
| --- | --- | --- |
| 定义接口 | `@GetMapping` | `@app.get(...)` |
| 路径参数 | `@PathVariable` | 函数参数 + 类型注解 |
| 请求体 | `@RequestBody` DTO | pydantic 模型参数 |
| 校验 | `@Valid` | pydantic 自动校验 |
| 依赖注入 | 构造注入 Bean | `Depends(...)` |
| 自动文档 | springdoc | 内建 `/docs` |

## 一个最小 REST 接口

```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI()

# pydantic 模型：声明字段和类型，FastAPI 自动据此校验请求体、生成文档。
# 类型不对、缺必填字段会自动返回 422，不用你手写校验。
class CreateEffectRequest(BaseModel):
    name: str
    category: str

class EffectResponse(BaseModel):
    id: int
    name: str
    category: str

# 路径里的 {id} 会按函数参数的类型注解（int）自动转换和校验
@app.get("/v1/effects/{id}", response_model=EffectResponse)
def get_effect(id: int):
    effect = repository.find(id)
    if effect is None:
        # 抛 HTTPException 自动转成对应状态码的 JSON 错误
        raise HTTPException(status_code=404, detail="effect not found")
    return effect

@app.post("/v1/effects", response_model=EffectResponse, status_code=201)
def create_effect(req: CreateEffectRequest):
    # req 已经过校验，直接用
    return repository.create(req)
```

- 启动：`uvicorn main:app --reload`，然后打开 `http://localhost:8000/docs` 就有自动生成的交互式文档。

## async 是 FastAPI 的重头戏

- 把上面的 `def` 换成 `async def`，就能在接口里 `await` 异步操作（调下游 API、异步 IO），单进程内并发处理大量等待型请求。

```python
import httpx

@app.get("/v1/aggregate")
async def aggregate():
    async with httpx.AsyncClient() as client:
        # await 时，事件循环可以去处理别的请求，不会干等着
        r = await client.get("https://downstream/api")
        return r.json()
```

- 这点对你的 AIGC 编排场景很关键：调多个 AI 服务大量时间在等网络，async 能让一个进程同时推进很多个等待中的任务。并发模型细节见下一篇。

## 依赖注入：Depends

- FastAPI 用 `Depends` 做依赖注入：声明“我需要这个东西”，框架负责准备好传进来。常用于拿数据库连接、当前登录用户、公共参数。

```python
from fastapi import Depends

def get_current_user(token: str = Header(...)):
    user = verify_token(token)        # 校验 token
    if user is None:
        raise HTTPException(401)
    return user

@app.get("/v1/me")
def me(user = Depends(get_current_user)):  # 框架先跑 get_current_user，再把结果注入
    return user
```

## Flask / Django 对比，什么时候选谁

- Flask：极简、灵活，什么都自己装。适合小服务、对结构要求不高、想完全掌控。
- Django：大而全（自带 ORM、admin 后台、auth），约定多。适合内容型/管理后台型应用，开发快但偏重。
- FastAPI：现代、async、类型驱动、自动文档。适合写 API 服务、需要异步、对接 AI/数据服务。
- 选型直觉：写 API（尤其要 async、对接 AI）选 FastAPI；要全家桶后台选 Django；要极简灵活选 Flask。

## Python vs Java，什么时候用哪个

- 用 Java/Spring：核心业务系统、强类型大团队协作、高并发稳定服务、生态成熟（你公司主力场景）。
- 用 Python/FastAPI：和 AI/数据/算法贴得近的服务、需要快速迭代、复用现有 Python 生态（你的 AIGC 链路）。
- 现实中常常混用：核心业务 Java，AI 编排和算法服务 Python，之间用 HTTP/gRPC 通信。

## 生产部署注意

- 开发用 `--reload`，生产别用。生产用 `uvicorn`/`gunicorn` 起多个 worker 进程（因为 Python 有 GIL，单进程吃不满多核，靠多进程扩展），前面再放网关。
- 这点和 Java 不同：JVM 一个进程内多线程就能吃满多核，Python 通常靠多进程。

## 小结

- FastAPI = async + 类型驱动校验 + 自动文档，概念和 Spring 一一对应，只是语法不同。
- async 让一个进程高效处理大量“等待型”请求，契合 AIGC 编排。
- Java 管核心业务，Python 管 AI/数据相邻的服务，混用很常见。
- 可运行示例见 `examples/06-fastapi-rest`。
