package io.wispforest.affinity.compat.igi;

import io.wispforest.affinity.component.AffinityComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

/**
 * Per-tick sampling cache backing the InGame Info XML tags.
 * <p>
 * IGI calls {@link df11zomgraves.ingameinfo.tag.Tag#getValue()} once per rendered line per
 * frame, so every value exposed here is sampled on the client tick instead and merely read
 * back during rendering
 */
@Environment(EnvType.CLIENT)
public final class AffinityIgiCache {

    /**
     * Window over which the player's aethum change is averaged
     */
    private static final int HISTORY_TICKS = 100;

    private static final double[] HISTORY = new double[HISTORY_TICKS];
    private static int historySize = 0;
    private static int historyHead = 0;

    private static double playerAethum = 0;
    private static double playerMaxAethum = 0;
    private static double playerDeltaPerSecond = 0;
    private static double chunkAethum = 0;

    private AffinityIgiCache() {}

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(AffinityIgiCache::tick);
    }

    private static void tick(MinecraftClient client) {
        var player = client.player;
        if (player == null) {
            reset();
            return;
        }

        final var component = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        playerMaxAethum = component.maxAethum();

        // the component walks its value up to the maximum in small increments every tick,
        // so the raw value jitters by fractions of a point -- snap it for display purposes
        final double current = component.getAethum();
        playerAethum = playerMaxAethum - current < .05 ? playerMaxAethum : current;

        samplePlayerAethum(playerAethum);
        playerDeltaPerSecond = averageDeltaPerSecond();

        final var world = player.getWorld();
        if (world == null) {
            chunkAethum = 0;
            return;
        }

        // equivalent to the server-side lookup used by the aethum probe: the chunk component
        // is synced to every player tracking that chunk, so this reads real data on the client
        final var chunk = world.getChunk(player.getBlockPos());
        chunkAethum = chunk.getComponent(AffinityComponents.CHUNK_AETHUM).getAethum();
    }

    private static void samplePlayerAethum(double value) {
        HISTORY[historyHead] = value;
        historyHead = (historyHead + 1) % HISTORY_TICKS;

        if (historySize < HISTORY_TICKS) historySize++;
    }

    /**
     * @return the average change per second across the samples collected so far, which is the
     * sampled window once it has filled up and a shorter window before that
     */
    private static double averageDeltaPerSecond() {
        if (historySize < 2) return 0;

        final int newestIndex = (historyHead - 1 + HISTORY_TICKS) % HISTORY_TICKS;
        final int oldestIndex = historySize == HISTORY_TICKS
                ? historyHead
                : (historyHead - historySize + HISTORY_TICKS) % HISTORY_TICKS;

        final double change = HISTORY[newestIndex] - HISTORY[oldestIndex];

        return change * 20d / (historySize - 1);
    }

    private static void reset() {
        historySize = 0;
        historyHead = 0;
        playerAethum = 0;
        playerMaxAethum = 0;
        playerDeltaPerSecond = 0;
        chunkAethum = 0;
    }

    // -------
    // Access
    // -------

    public static double playerAethum() {
        return playerAethum;
    }

    public static double playerMaxAethum() {
        return playerMaxAethum;
    }

    public static double playerDeltaPerSecond() {
        return playerDeltaPerSecond;
    }

    public static double chunkAethum() {
        return chunkAethum;
    }

    /**
     * @return the chunk the player currently occupies, or {@code null} when not in a world
     */
    public static ChunkPos playerChunkPos() {
        var player = MinecraftClient.getInstance().player;
        return player == null ? null : player.getChunkPos();
    }

}
