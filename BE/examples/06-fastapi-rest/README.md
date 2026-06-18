# 06 · FastAPI 最小 REST 示例

- 对应文档：`BE/06-fastapi.md`
- 演示：分层、pydantic 自动校验、依赖注入（Depends）、统一错误、自动 OpenAPI 文档、健康检查。

## 运行

```bash
pip install -r requirements.txt
uvicorn main:app --reload
```

- 打开 http://localhost:8000/docs ——这是 FastAPI 自动生成的交互式接口文档，可以直接在页面上调接口。

## 试一试

```bash
# 新建（返回 201）
curl -X POST localhost:8000/v1/effects -H 'Content-Type: application/json' \
  -d '{"name":"溶解转场","category":"transition"}'

# 列表
curl localhost:8000/v1/effects

# 取单个
curl localhost:8000/v1/effects/1

# 取不存在的（返回 404 + JSON 错误体）
curl -i localhost:8000/v1/effects/999

# 校验失败（name 为空，返回 422）
curl -i -X POST localhost:8000/v1/effects -H 'Content-Type: application/json' \
  -d '{"name":"","category":"x"}'
```

## 对照 Spring Boot

- 同样的接口在 `../05-spring-boot-rest` 用 Java/Spring 实现，可对比两套写法的异同。
