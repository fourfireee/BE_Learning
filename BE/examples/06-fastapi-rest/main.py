"""
FastAPI 最小 REST 示例：分层、pydantic 校验、依赖注入、统一错误、自动文档。

对应文档：BE/06-fastapi.md

运行：
    pip install -r requirements.txt
    uvicorn main:app --reload
然后打开 http://localhost:8000/docs 看自动生成的交互式文档。
"""

from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Effect Service (demo)")


# ---- DTO：用 pydantic 声明请求/响应结构，框架自动校验和生成文档 ----
class CreateEffectRequest(BaseModel):
    # Field 约束会被自动校验：name 1~100 字符，category 必填
    name: str = Field(min_length=1, max_length=100)
    category: str = Field(min_length=1, max_length=50)


class EffectResponse(BaseModel):
    id: int
    name: str
    category: str


# ---- “Repository”：示例用内存字典假装数据库 ----
class EffectRepository:
    def __init__(self) -> None:
        self._data: dict[int, EffectResponse] = {}
        self._seq = 0

    def create(self, req: CreateEffectRequest) -> EffectResponse:
        self._seq += 1
        e = EffectResponse(id=self._seq, name=req.name, category=req.category)
        self._data[e.id] = e
        return e

    def find(self, id: int) -> EffectResponse | None:
        return self._data.get(id)

    def list(self) -> list[EffectResponse]:
        return list(self._data.values())


repo = EffectRepository()


# 依赖注入：声明“我需要一个 repo”，框架准备好传进来。
# 真实项目里这里会返回数据库会话/连接。
def get_repo() -> EffectRepository:
    return repo


@app.post("/v1/effects", response_model=EffectResponse, status_code=201)
def create_effect(req: CreateEffectRequest, repo: EffectRepository = Depends(get_repo)):
    # req 已通过校验，直接用；返回 201 表示新建成功
    return repo.create(req)


@app.get("/v1/effects", response_model=list[EffectResponse])
def list_effects(repo: EffectRepository = Depends(get_repo)):
    return repo.list()


@app.get("/v1/effects/{id}", response_model=EffectResponse)
def get_effect(id: int, repo: EffectRepository = Depends(get_repo)):
    effect = repo.find(id)
    if effect is None:
        # 抛 HTTPException 自动转成 404 的 JSON 错误体
        raise HTTPException(status_code=404, detail="effect not found")
    return effect


@app.get("/health")
def health():
    # 健康检查接口，供负载均衡/K8s 探测（见部署篇）
    return {"status": "ok"}
