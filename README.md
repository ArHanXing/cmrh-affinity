# Cmrh-Affinity
又一个为了 CmRhPack 而 Vibe-coding 的 fork

## 与植物魔法（Botania）的互通

本 fork 增加了生灵通量与 Botania 魔力的双向转化，Botania 为**可选依赖**：未安装时相关代码
完全不会被加载，模组行为与上游一致。

### 转化比率

`2 魔力 = 1 生灵通量`，反之为 `1 生灵通量 = 2 魔力`。该比率以两者的入门产能对齐
（余烬花约 60 魔力/s，物质收获炉约 30 通量/s），即一个入门级发电机在两边大致等价。

### 两个方向

| 方向 | 做法 |
|---|---|
| 魔力 → 生灵通量 | 用**魔力发射器**对准**生灵通量缓存**发射脉冲，命中即按比率转化为通量存入；高塔缓存自下而上填充 |
| 生灵通量 → 魔力 | 用**彩虹法杖**把**通量节点**与任意 Botania 魔力方块连接（先点哪个都行），节点即远程为其供能 |

第二个方向不是特例实现：`io.wispforest.affinity.compat.botania.BotaniaManaMember` 把 Botania
方块作为**普通网络成员**注册进 `AETHUM_MEMBER` 查找表，因此链接、远程传输、push/优先链接、
碎片等级限速等全部直接复用原生逻辑，魔力池、发射器、符文祭坛、大地聚合板等**任何实现
`ManaReceiver` 的方块**都能被供能。

### 配置项（`config/affinity.json5`，`botania` 段）

| 键 | 默认 | 说明 |
|---|---|---|
| `botaniaManaIntegration` | `true` | 总开关，关闭后所有互通行为失效 |
| `botaniaManaPerFlux` | `2` | 转化比率，即 1 通量相当于多少魔力 |
| `botaniaFluxTransferPerTick` | `10` | 单条链接每 tick 最多搬运的通量，即 20 魔力/tick（400 魔力/s） |
| `botaniaManaExtraction` | `false` | 是否允许反向抽取：仅对处于**输出模式**的魔力池生效 |

### 实现说明

- 相关代码全部集中在 `src/main/java/io/wispforest/affinity/compat/botania/`，由
  `Affinity#onInitialize` 中的 `isModLoaded("botania")` 守卫。
- Botania 的 api 以**只编译的镜像**形式提供，见 [`src/botaniaApi/README.md`](src/botaniaApi/README.md)
  （其 Fabric 产物由 Loom 1.14 构建，本项目停留在 Loom 1.8，直接依赖会同时触发版本校验失败
  与 remap 冲突）。镜像签名已用 `javap` 对真实 jar 逐条核对。

---

# Cmrh-Affinity (upstream fork)

A fork of [Affinity](https://github.com/wisp-forest/affinity) for **CmRhPack**, adding the
Botania mana interop described above on top of the upstream mod.
