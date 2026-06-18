"""
特效存储与下发最小示例：元数据在“库”里，下发用签名 URL，应用不当文件中转。

对应文档：BE/20-实战A-素材存储与下发.md

为了能独立跑，这里把对象存储用一个本地假实现 (FakeStorage) 顶替：
- presigned_put / presigned_get 返回带签名和过期时间的“链接”（演示用，不是真能下载的地址）。
真实项目把 FakeStorage 换成 MinIO/S3 客户端即可（README 有说明），接口形状一致。

运行：
    pip install -r requirements.txt
    uvicorn main:app --reload
"""

import hashlib
import time

from fastapi import Depends, FastAPI, Header, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Effect Delivery (demo)")


# ---- 假的对象存储：演示签名 URL 的“形状”，真实换成 S3/MinIO SDK ----
class FakeStorage:
    SECRET = "demo-signing-secret"

    def _sign(self, key: str, expires_at: int) -> str:
        raw = f"{key}:{expires_at}:{self.SECRET}".encode()
        return hashlib.sha256(raw).hexdigest()[:16]

    def presigned_get(self, key: str, expires: int = 300) -> str:
        exp = int(time.time()) + expires
        sig = self._sign(key, exp)
        # 真实场景这是 CDN/对象存储的直连下载地址，客户端拿它直接下，不经过本应用
        return f"https://cdn.example.com/{key}?expires={exp}&sig={sig}"

    def presigned_put(self, key: str, expires: int = 300) -> str:
        exp = int(time.time()) + expires
        sig = self._sign(key, exp)
        return f"https://oss.example.com/{key}?expires={exp}&sig={sig}&method=PUT"


storage = FakeStorage()


# ---- 假的元数据库 ----
class Effect(BaseModel):
    id: int
    name: str
    category: str
    version: int
    object_key: str
    status: str = "published"


EFFECTS: dict[int, Effect] = {
    1: Effect(id=1, name="溶解转场", category="transition", version=2,
              object_key="effects/1/v2/dissolve.zip"),
    2: Effect(id=2, name="故障风", category="transition", version=1,
              object_key="effects/2/v1/glitch.zip"),
}


# 极简鉴权：演示“发下载链接前要鉴权”。真实见鉴权篇。
def current_user(authorization: str = Header(default="")) -> str:
    if not authorization.startswith("Bearer "):
        raise HTTPException(401, "missing token")
    return authorization.removeprefix("Bearer ")


@app.get("/v1/effects")
def list_effects(category: str | None = None):
    items = [e for e in EFFECTS.values() if e.status == "published"]
    if category:
        items = [e for e in items if e.category == category]
    return {"items": items}


@app.get("/v1/effects/{id}")
def get_effect(id: int):
    e = EFFECTS.get(id)
    if e is None or e.status != "published":
        raise HTTPException(404, "effect not found")
    return e


@app.get("/v1/effects/{id}/download-url")
def download_url(id: int, user: str = Depends(current_user)):
    e = EFFECTS.get(id)
    if e is None or e.status != "published":
        raise HTTPException(404, "effect not found")
    # 鉴权/付费校验就发生在“发链接”这一步（这里简化为已登录即可）
    # 生成带时效的签名 URL，客户端拿它直连 CDN 下载，流量不压本应用
    url = storage.presigned_get(e.object_key, expires=300)
    return {"url": url, "expiresIn": 300}


@app.post("/v1/effects:upload-url")
def upload_url(key: str, user: str = Depends(current_user)):
    # 运营侧：拿签名上传 URL，直传对象存储（大文件再配分片上传）
    return {"url": storage.presigned_put(key, expires=600), "expiresIn": 600}
