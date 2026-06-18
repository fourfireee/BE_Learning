# 05 · Spring Boot 最小 REST 示例

- 对应文档：`BE/05-spring-boot.md`
- 演示：依赖注入、Controller/Service/Repository 分层、DTO（record）、@Valid 校验、统一异常处理、201/404 状态码。
- 和 `../06-fastapi-rest` 是同一组接口的 Java 版，可对照阅读。

## 前置

- JDK 21、Maven。（没装 Maven 也可用 IDE 直接运行 `EffectApplication`。）

## 运行

```bash
mvn spring-boot:run
```

服务起在 `http://localhost:8080`。

## 试一试

```bash
# 新建（返回 201）
curl -X POST localhost:8080/v1/effects -H 'Content-Type: application/json' \
  -d '{"name":"溶解转场","category":"transition"}'

# 列表
curl localhost:8080/v1/effects

# 取单个
curl localhost:8080/v1/effects/1

# 不存在（统一错误体 + 404）
curl -i localhost:8080/v1/effects/999

# 校验失败（name 为空，返回 400）
curl -i -X POST localhost:8080/v1/effects -H 'Content-Type: application/json' \
  -d '{"name":"","category":"x"}'
```

## 代码导读

- `EffectApplication`：入口，启动容器并扫描组件。
- `EffectController`：接 HTTP、出 JSON，只做翻译，不放业务。
- `EffectService`：业务逻辑，构造注入 Repository。
- `EffectRepository`：数据访问（示例用内存 Map 假装数据库）。
- `Dtos`：请求/响应/错误结构，用 record。
- `GlobalExceptionHandler`：把业务异常集中转成统一错误体 + 对应状态码。

## 对照 FastAPI

- 注意 Java 这边对象由容器创建注入、强类型显式；FastAPI 那边靠类型注解 + Depends，更简洁。概念一一对应。
