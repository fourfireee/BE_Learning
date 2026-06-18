# 20 · 特效存储与下发最小示例

- 对应文档：`BE/20-实战A-素材存储与下发.md`
- 演示核心套路：元数据在库、下发用签名 URL、应用只发链接不当文件中转、发链接前鉴权。
- 为了独立可跑，对象存储用本地假实现顶替（签名 URL 只演示“形状”，不是真能下的地址）。

## 运行

```bash
pip install -r requirements.txt
uvicorn main:app --reload
```

## 试一试

```bash
# 列表 / 详情（无需登录）
curl localhost:8000/v1/effects
curl localhost:8000/v1/effects?category=transition
curl localhost:8000/v1/effects/1

# 拿下载链接：要带 token（演示用任意字符串）
curl localhost:8000/v1/effects/1/download-url -H 'Authorization: Bearer u123'
# => {"url":"https://cdn.example.com/effects/1/v2/dissolve.zip?expires=...&sig=...","expiresIn":300}

# 不带 token -> 401
curl -i localhost:8000/v1/effects/1/download-url
```

## 要点

- `object_key` 带版本（`effects/1/v2/...`）：素材更新时换版本即换 URL，天然绕开 CDN 旧缓存。
- 下载链接有 `expires` 和 `sig`，过期失效、可防盗链；发不发链接由应用在这一步鉴权决定。
- 应用只返回一个字符串，真正的下载流量走 CDN/对象存储，不压应用。

## 换成真实对象存储（MinIO/S3）

- 把 `FakeStorage` 换成 boto3（S3 兼容）即可，接口一致：

```python
import boto3
s3 = boto3.client("s3", endpoint_url="http://localhost:9000",
                  aws_access_key_id="...", aws_secret_access_key="...")
url = s3.generate_presigned_url("get_object",
        Params={"Bucket": "effects", "Key": key}, ExpiresIn=300)
```

- 本地起 MinIO（S3 兼容）：`docker run -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ':9001'`。
