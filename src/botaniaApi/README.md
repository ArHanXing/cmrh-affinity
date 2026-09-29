# botaniaApi source set

This source set contains a **hand written, compile-only mirror** of the small slice of
[Botania](https://github.com/VazkiiMods/Botania) 1.21.1 (Fabric) api that Affinity's
optional integration touches:

```
vazkii.botania.api.mana.ManaReceiver
vazkii.botania.api.mana.ManaPool
```

Nothing in here is part of the mod jar. The classes are only on the compile classpath of
`main`, and the real ones are supplied by Botania at runtime — this is exactly how a
`compileOnly` dependency behaves, just with a source set instead of an artifact.

## Why not `modCompileOnly`?

Botania's published fabric jar (`vazkii.botania:botania-fabric-1.21.1`, on
<https://maven.blamejared.com>) is built with Loom 1.14.7 and uses official Mojang
mappings. Depending on it from this project, which is on Loom 1.8 / Yarn, fails twice:

1. The jar's manifest carries `Fabric-Loom-Mixin-Remap-Type: static` (an attribute a
   dependency must not have) together with `Fabric-Loom-Version: 1.14.7`, which trips
   Loom 1.8's strict validation with *"Mod was built with a newer version of Loom"*.
   `loom.ignoreDependencyLoomVersionValidation` does not exist in Loom 1.8, so it cannot
   be turned off.
2. Even with those attributes stripped, remapping the whole mod from Mojang mappings to
   Yarn aborts on an unfixable method name conflict inside Botania itself
   (`BotaniaBookModel.render`).

Upgrading this project to Loom 1.14 to accommodate one optional integration is not worth
the churn, so the api surface is mirrored instead.

## Keeping it honest

The mirror must match the real interfaces exactly — a stale mirror would compile fine and
then fail at runtime with `AbstractMethodError`. It was transcribed from the actual
artifact:

```bash
curl -O https://maven.blamejared.com/vazkii/botania/botania-fabric-1.21.1/457-SNAPSHOT/botania-fabric-1.21.1-457-20260917.091203-2.jar
unzip -o botania-fabric-1.21.1-*.jar 'vazkii/botania/api/mana/*.class'
javap -p vazkii/botania/api/mana/ManaReceiver.class
javap -p vazkii/botania/api/mana/ManaPool.class
```

Re-run `javap` after every Botania update and amend these files if a signature changed.
Only the descriptors matter; parameter names and annotations are irrelevant.

## Runtime safety

Every reference to these classes is confined to `io.wispforest.affinity.compat.botania`,
which is only loaded when `FabricLoader.getInstance().isModLoaded("botania")` is true (see
`Affinity#onInitialize`). Without Botania installed the package is never touched, so the
missing classes can never cause a `NoClassDefFoundError`.
