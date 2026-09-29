package io.wispforest.affinity.compat.botania;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import io.wispforest.affinity.misc.callback.ForeignLinkableProvider;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.util.math.Direction;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * Entry point of the optional Botania integration. This is only ever loaded and
 * called when Botania is present, so every reference to its API in here is safe
 * <p>
 * The exchange rate between aethum flux and Botania mana is configurable and
 * defaults to <b>2 mana = 1 flux</b>, i.e. a node feeds {@code 2 * n} mana into
 * a Botania block for every {@code n} flux it spends
 */
public final class AffinityBotaniaCompat {

    /**
     * The very same lookup Botania creates for {@link ManaReceiver#LOOKUP} --
     * {@link BlockApiLookup#get} is idempotent, so this works regardless of whether
     * Botania has already initialized at this point
     */
    public static final BlockApiLookup<ManaReceiver, Direction> MANA_RECEIVER_LOOKUP =
            BlockApiLookup.get(ManaReceiver.ID, ManaReceiver.class, Direction.class);

    private AffinityBotaniaCompat() {}

    public static void initialize() {
        MANA_RECEIVER_LOOKUP.registerForBlockEntities(
                (blockEntity, context) -> blockEntity instanceof AethumFluxCacheBlockEntity cache
                        ? new AethumFluxCacheManaReceiver(cache)
                        : null,
                AffinityBlocks.Entities.AETHUM_FLUX_CACHE
        );

        // exposes every Botania mana block as a regular member of the aethum flux network,
        // which makes nodes able to link to and supply it just like a native machine
        Affinity.AETHUM_MEMBER.registerFallback((world, pos, state, blockEntity, context) -> {
            if (!Affinity.config().botaniaManaIntegration()) return null;

            var receiver = MANA_RECEIVER_LOOKUP.find(world, pos, state, blockEntity, null);
            return receiver == null ? null : new BotaniaManaMember(world, pos, receiver);
        });

        // ... and lets the wand of iridescence begin and destroy links on those blocks
        ForeignLinkableProvider.EVENT.register((world, pos, blockEntity) -> {
            if (!Affinity.config().botaniaManaIntegration()) return null;

            var receiver = MANA_RECEIVER_LOOKUP.find(world, pos, null);
            return receiver == null ? null : new BotaniaManaMember(world, pos, receiver);
        });
    }

}
