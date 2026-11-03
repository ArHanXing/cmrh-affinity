package io.wispforest.affinity.compat.igi;

import df11zomgraves.ingameinfo.api.InGameInfoAPI;
import df11zomgraves.ingameinfo.tag.Tag;
import io.wispforest.affinity.Affinity;

import java.util.Locale;

/**
 * Optional integration with InGame Info XML: exposes the player's aethum, its rate of change
 * and the aethum of the chunk the player is standing in as {@code {affinity_*}} tags.
 * <p>
 * Only ever loaded and called when InGame Info XML is present, so the references to its api
 * in here are safe
 */
public final class AffinityIgiCompat {

    private static final String TAG_PREFIX = "affinity_";

    private AffinityIgiCompat() {}

    public static void initialize() {
        AffinityIgiCache.initialize();

        register("player_aethum", TagPlayerAethum::new);
        register("player_aethum_max", TagPlayerAethumMax::new);
        register("player_aethum_rate", TagPlayerAethumRate::new);
        register("chunk_aethum", TagChunkAethum::new);

        Affinity.LOGGER.info("[affinity] registered InGame Info XML tags: {}player_aethum, {}player_aethum_max, {}player_aethum_rate, {}chunk_aethum",
                TAG_PREFIX, TAG_PREFIX, TAG_PREFIX, TAG_PREFIX);
    }

    private static void register(String name, java.util.function.Supplier<Tag> factory) {
        final String fullName = TAG_PREFIX + name;

        if (InGameInfoAPI.isTagRegistered(fullName)) {
            Affinity.LOGGER.warn("[affinity] InGame Info XML tag {} is already registered, skipping", fullName);
            return;
        }

        InGameInfoAPI.registerTag(factory.get().setName(fullName));
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String formatSigned(double value) {
        return String.format(Locale.ROOT, "%+.2f", value);
    }

    /**
     * {@code {affinity_player_aethum}} - how much aethum the player currently has
     */
    public static class TagPlayerAethum extends Tag {

        @Override
        public String getCategory() {
            return "affinity";
        }

        @Override
        public String getValue() {
            return format(AffinityIgiCache.playerAethum());
        }

    }

    /**
     * {@code {affinity_player_aethum_max}} - the player's current aethum capacity
     */
    public static class TagPlayerAethumMax extends Tag {

        @Override
        public String getCategory() {
            return "affinity";
        }

        @Override
        public String getValue() {
            return format(AffinityIgiCache.playerMaxAethum());
        }

    }

    /**
     * {@code {affinity_player_aethum_rate}} - average aethum change per second over the last
     * five seconds, signed, e.g. {@code +0.03} or {@code -1.20}
     */
    public static class TagPlayerAethumRate extends Tag {

        @Override
        public String getCategory() {
            return "affinity";
        }

        @Override
        public String getValue() {
            return formatSigned(AffinityIgiCache.playerDeltaPerSecond());
        }

    }

    /**
     * {@code {affinity_chunk_aethum}} - the aethum of the chunk the player is standing in
     */
    public static class TagChunkAethum extends Tag {

        @Override
        public String getCategory() {
            return "affinity";
        }

        @Override
        public String getValue() {
            return format(AffinityIgiCache.chunkAethum());
        }

    }

}
