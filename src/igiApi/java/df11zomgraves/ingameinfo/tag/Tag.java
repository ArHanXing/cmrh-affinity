/*
 * Compile-time mirror of df11zomgraves.ingameinfo.tag.Tag (InGame Info XML 2.9.2.101, Fabric).
 *
 * Transcribed from the real source at
 *   InGameInfoXML-Fabric/fabric/src/main/java/df11zomgraves/ingameinfo/tag/Tag.java
 *
 * The real class also carries protected static `minecraft` / `world` / `player` fields; they
 * are deliberately omitted here so that this mirror needs no minecraft classes on its
 * classpath. The integration never reads those fields -- it goes through
 * MinecraftClient.getInstance() instead, which is equivalent.
 *
 * This file is NOT part of the mod: it lives in the compile-only `igiApi` source set,
 * is never bundled into the jar and is never loaded at runtime. At runtime the real class
 * is supplied by InGame Info XML, which is an optional dependency.
 */
package df11zomgraves.ingameinfo.tag;

public abstract class Tag {

    private String name = null;

    public Tag setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Tag setAliases(final String... aliases) {
        return this;
    }

    public boolean isIndexed() {
        return false;
    }

    public int getMaximumIndex() {
        return -1;
    }

    public abstract String getCategory();

    public abstract String getValue();

}
