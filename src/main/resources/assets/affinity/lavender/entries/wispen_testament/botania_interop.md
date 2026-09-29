```json
{
  "title": "植物魔法互通",
  "icon": "affinity:aethum_flux_cache",
  "category": "affinity:aethum_flux_networks",
  "required_advancements": [
    "affinity:some_assembly_required"
  ],
  "associated_items": [
    "affinity:aethum_flux_cache",
    "affinity:stone_banded_aethum_flux_node"
  ]
}
```

生灵通量与{item}植物魔法{}的{concept}魔力{}本质上是同一种东西的两种形态，因此两者之间可以{concept}双向转化{}。
转化比率为 {concept}2 魔力 = 1 生灵通量{}，反过来则是 {concept}1 生灵通量 = 2 魔力{}。

;;;;;

**魔力 → 生灵通量**

只需要把{item}魔力发射器{}对准{item}生灵通量缓存{}即可：魔力脉冲命中缓存时，其中的魔力会被转化并存入缓存。
高塔状的缓存会{concept}自下而上{}依次填充。


;;;;;

**生灵通量 → 魔力**

{concept}所有实现魔力接收的方块都可以被供能{}——无论是{item}魔力池{}、{item}魔力发射器{}、{item}符文祭坛{}，还是{item}大地聚合板{}。


这些方块在通量网络中的表现与原生机械{concept}完全一致{}：手持[彩虹法杖](^affinity:wand_of_iridescence)先点击{item}节点{}再点击魔力方块（反过来也可以）即可建立连接。
之后节点就会{concept}远程{}向它输送魔力，传输速率同样受{item}谐振碎片{}等级限制，因此升级碎片就能提高供能速度。


;;;;;

**反向抽取**

默认情况下，通量只会{concept}流入{}魔力方块。
如果希望反过来把魔力池中的魔力抽入通量网络，需要在配置文件中开启 {concept}botaniaManaExtraction{}，并且把该魔力池切换到{concept}输出模式{}。

@entry-end
