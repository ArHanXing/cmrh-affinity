# 本项目由 AI 生成
# Cmrh-Affinity
又一个为了 CmRhPack 而 Vibe-coding 的 fork

## 与植物魔法（Botania）的互通 （9.29）
Model: agentrouter@deepseek-v4-flash

增加了生灵通量（AF）与 Botania 魔力（mana）的双向转化。

由于沟槽的 Botania 使用 mojmap，ai的实现我看都不敢看。

### 默认转化比率

`2 魔力 = 1 生灵通量`，反之为 `1 生灵通量 = 2 魔力`。该比率以两者的入门产能对齐
（余烬花约 60 魔力/s，物质收获炉约 30 通量/s），即一个入门级发电机在两边大致等价。

### 配置项（`config/affinity.json5`，`botania` 段）

| 键 | 默认 | 说明 |
|---|---|---|
| `botaniaManaIntegration` | `true` | 互通开关 |
| `botaniaManaPerFlux` | `2` | 转化比率 |
| `botaniaFluxTransferPerTick` | `10` | 单条链接每 tick 最多搬运的通量，即 20 mana/tick（400 mana/s） |
| `botaniaManaExtraction` | `false` | 是否允许反向抽取：仅对处于**输出模式**的魔力池生效 |

### 实现说明

- 相关代码全部集中在 `src/main/java/io/wispforest/affinity/compat/botania/`
- Botania 的 api 以**只编译的镜像**形式提供，见 [`src/botaniaApi/README.md`](src/botaniaApi/README.md)
