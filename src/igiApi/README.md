# igiApi source set

This source set contains a **hand written, compile-only mirror** of the small slice of
[InGame Info XML](https://github.com/df11zomgraves/InGameInfoXML-Fabric) 2.9.2.101
(Fabric) api that Affinity's optional integration touches:

```
df11zomgraves.ingameinfo.api.InGameInfoAPI
df11zomgraves.ingameinfo.tag.Tag
```

Nothing in here is part of the mod jar. These classes are only on the compile classpath of
`main`, and the real ones are supplied by InGame Info XML at runtime — exactly how a
`compileOnly` dependency behaves, just with a source set instead of an artifact.

## Why not `modCompileOnly`?

InGame Info XML's published fabric jar is built with Loom 1.17.19 using official Mojang
mappings, and its manifest carries `Fabric-Loom-Mixin-Remap-Type: static` together with
`Fabric-Loom-Version: 1.17.19`. Depending on it from this project, which is on Loom 1.8 /
Yarn, fails for the same two reasons as Botania (see `../botaniaApi/README.md`):

1. Loom 1.8's strict dependency validation rejects a jar built by newer Loom, and
   `loom.ignoreDependencyLoomVersionValidation` does not exist in Loom 1.8.
2. Remapping the whole mod from Mojang mappings to Yarn is not guaranteed to succeed.

The integration only needs three static methods and an abstract base class, so mirroring
them is by far the cheaper option.

## Deliberately minecraft-free

The real `Tag` class also declares `protected static Minecraft minecraft`,
`ClientLevel world` and `LocalPlayer player`. Those fields are **omitted** from the mirror
so that it needs no minecraft classes on its classpath at all. Nothing is lost: the
integration reads the player through `MinecraftClient.getInstance()` instead, which is
equivalent (`Tag.player` is merely refreshed from it every client tick).

## Keeping it honest

The mirror must match the real classes exactly — a stale mirror compiles fine and then
fails at runtime with `AbstractMethodError` or `NoSuchMethodError`. It was transcribed from
the actual artifact:

```bash
unzip -o ingameinfoxml-2.9.2.101.jar \
  'df11zomgraves/ingameinfo/tag/Tag.class' \
  'df11zomgraves/ingameinfo/api/InGameInfoAPI.class'
javap -p df11zomgraves/ingameinfo/tag/Tag.class
javap -p df11zomgraves/ingameinfo/api/InGameInfoAPI.class
```

Re-run `javap` after every InGame Info XML update and amend these files if a signature
changed. Only descriptors matter; parameter names and annotations are irrelevant.

## Runtime safety

Every reference to these classes is confined to `io.wispforest.affinity.compat.igi`, which
is only loaded when `FabricLoader.getInstance().isModLoaded("ingameinfoxml")` is true (see
`AffinityClient#onInitializeClient`). Without InGame Info XML installed the package is
never touched, so the missing classes can never cause a `NoClassDefFoundError`.
