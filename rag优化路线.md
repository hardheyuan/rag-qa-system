# RAG优化路线（低实现难度版）

更新时间：2026-02-08

这版路线专门按你现在的节奏来：

- 小步快跑（每步 0.5-1 天）
- 每步只改 1-2 个文件
- 能快速看到效果，不做大重构

---

## 1. 先定目标（简单可验收）

本轮只追 3 个结果：

1. 回答更容易命中文档（减少“答非所问”）
2. 文档处理更快（上传后等待更短）
3. 改动可回滚（vibe coding 友好）

验收看这 3 个数字就行：

- 命中引用比例（有 citations 的回答占比）
- 文档处理耗时（从上传到 SUCCESS）
- 问答平均耗时

---

## 2. 最推荐执行顺序（从易到难）

## Step 1（今天可做）：只改配置，不改逻辑

目标：先用最小改动拿一波稳定收益。

改动建议：

- `qa.top-k`：5 -> 8（先别拉太高）
- `document.chunk-size`：1000 -> 800
- `document.chunk-overlap`：100 -> 120
- `async.core-pool-size`：2 -> 4
- `async.max-pool-size`：5 -> 8

涉及文件：

- `demo1/src/main/resources/application.yml`

为什么先做这个：

- 风险低
- 改完马上可测
- 出问题一键回滚

---

## Step 2（0.5 天）：修检索度量不一致（高收益低代码量）

你当前代码里，索引是 cosine，但查询排序用了 L2（`<->`）。这会影响检索质量。

改动建议：

- 把查询里的 `<->` 改成 `<=>`，统一成 cosine 距离

涉及文件：

- `demo1/src/main/java/com/hiyuan/demo1/repository/VectorRecordRepository.java`

验收：

- 随机测 20 个问题，观察引用是否更贴题

---

## Step 3（1 天）：把“固定 sleep 1 秒”改成“批量向量化”

你项目里已经有 `embedAll`，但主流程还在逐条 `embed + sleep(1000)`。

改动建议：

- 在 `saveChunksAndVectors` 里按 16 条一批调用 `embedAll`
- 删除固定 `Thread.sleep(1000)`
- 保留失败重试逻辑（已有）

涉及文件：

- `demo1/src/main/java/com/hiyuan/demo1/service/DocumentProcessorService.java`
- `demo1/src/main/java/com/hiyuan/demo1/service/EmbeddingService.java`

验收：

- 同一份文档上传处理时间明显下降（通常会快很多）

---

## Step 4（0.5 天）：把阈值过滤改得更稳一点

当前固定阈值 0.65 在某些问题上会“过滤过头”。

改动建议（选简单版）：

- 仍保留阈值 0.65
- 但增加“保底 2 条引用”规则：如果过滤后少于 2 条，就补前 2 条候选

涉及文件：

- `demo1/src/main/java/com/hiyuan/demo1/service/QaService.java`

验收：

- “无引用回答”数量下降

---

## Step 5（可选，1 天）：加最小监控，不上复杂平台

不引入新系统，先用日志就够。

记录 4 个值：

- top-k 命中数
- 过滤后引用数
- 问答耗时
- 文档处理耗时

涉及文件：

- `demo1/src/main/java/com/hiyuan/demo1/service/QaService.java`
- `demo1/src/main/java/com/hiyuan/demo1/service/DocumentProcessorService.java`

---

## 3. 明确这轮不做（防止难度爆炸）

这轮先不做以下内容：

- 不上 reranker
- 不做混合检索（BM25 + 向量）
- 不改数据库架构
- 不引入 Redis
- 不切换模型

理由：这些都能提升，但实现复杂度明显更高，不适合你现在“快速迭代”的节奏。

---

## 4. 两周落地计划（轻量）

- 第 1-2 天：Step 1 + Step 2
- 第 3-5 天：Step 3
- 第 6 天：Step 4
- 第 7-10 天：观察数据 + 微调参数

只要完成 Step 1-4，通常就能拿到一版“明显可感知”的优化。

---

## 5. 最小成功标准（这版就看这个）

达到以下任意 2 条，就算这一轮成功：

- 文档处理时间下降 30%+
- 有引用回答占比提升 15%+
- 问答平均耗时下降 15%+

如果这版跑顺了，再考虑第二轮（rerank、混合检索等进阶项）。
