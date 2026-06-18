# 14 · 集群 + 网关 + 共享状态 最小可跑示例

- 对应文档：`BE/14-集群负载均衡与网关.md`
- 一条命令起一个最小集群：Nginx 网关 + 3 个应用实例 + 共享 Redis。
- 用它直观看到两件事：负载均衡如何分流、状态外置后任意实例计数都连续。

## 前置

- 装了 Docker 和 docker compose 即可，不用本地装 Python。

## 启动

```bash
docker compose up --build
```

## 观察负载均衡（请求被分给不同实例）

```bash
# 多打几次，看 servedBy 在 app1/app2/app3 之间轮换（Nginx 轮询）
for i in $(seq 1 6); do curl -s localhost:8080/api/hit; echo; done
```

输出类似：

```json
{"servedBy":"app1","globalHits":1}
{"servedBy":"app2","globalHits":2}
{"servedBy":"app3","globalHits":3}
{"servedBy":"app1","globalHits":4}
```

## 体会“状态外置”

- `servedBy` 在变（请求落到不同实例），但 `globalHits` 始终连续递增——因为计数器在共享的 Redis 里，不在某个实例的内存里。
- 这正是第 1 篇“无状态 + 状态外置”的意义：实例可随意增减、请求随便分，状态都对得上。

## 体会高可用

```bash
# 干掉一个实例，再继续请求，服务照常（只是不再被分到 app2）
docker compose stop app2
for i in $(seq 1 6); do curl -s localhost:8080/api/hit; echo; done
```

## 拓扑

```
            (宿主机 :8080)
                  |
            [ Nginx 网关 ]
            /     |      \
        app1    app2    app3      <- 3 个无状态应用实例
            \     |      /
              [  Redis  ]          <- 共享状态(计数器)
```

## 清理

```bash
docker compose down
```
