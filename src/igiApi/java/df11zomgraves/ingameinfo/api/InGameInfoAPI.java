/*
 * Compile-time mirror of df11zomgraves.ingameinfo.api.InGameInfoAPI (InGame Info XML 2.9.2.101, Fabric).
 *
 * Transcribed from the real source at
 *   InGameInfoXML-Fabric/fabric/src/main/java/df11zomgraves/ingameinfo/api/InGameInfoAPI.java
 *
 * Only the handful of methods this integration calls are mirrored. See Tag.java in this
 * source set for why the mirror exists at all.
 */
package df11zomgraves.ingameinfo.api;

import df11zomgraves.ingameinfo.tag.Tag;

public final class InGameInfoAPI {

    private InGameInfoAPI() {}

    /**
     * Registers a tag. May be called at any point in the mod lifecycle, including before the
     * built-in tags are initialised, in which case the registration is queued.
     */
    public static void registerTag(final Tag tag) {
        throw new UnsupportedOperationException("compile-only mirror");
    }

    /**
     * @return whether the name is already taken; names are matched case-insensitively
     */
    public static boolean isTagRegistered(final String name) {
        throw new UnsupportedOperationException("compile-only mirror");
    }

    /**
     * @return whether a mod with the given id is loaded
     */
    public static boolean isModLoaded(final String modId) {
        throw new UnsupportedOperationException("compile-only mirror");
    }

}
