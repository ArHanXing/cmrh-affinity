package io.wispforest.affinity.compat.botania;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.AethumFluxCacheBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.Comparator;
import java.util.List;

/**
 * Makes an aethum flux cache a valid target for Botania mana bursts, so spreaders can
 * charge it from a distance. Mana is converted to flux and handed to the cache, which
 * means a tall cache is filled bottom-up
 * <p>
 * A burst delivers its mana in a single instant, so unlike a machine the cache does not
 * throttle the intake by its shard tier -- the tier keeps governing how fast the network
 * may pull the flux back out, and the cache's capacity is the only limit on what goes in
 */
public record AethumFluxCacheManaReceiver(AethumFluxCacheBlockEntity cache) implements ManaReceiver {

    private static int ratio() {
        return Math.max(1, Affinity.config().botaniaManaPerFlux());
    }

    @Override
    public World getManaReceiverLevel() {
        return this.cache.getWorld();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return this.cache.getPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, this.cache.displayFlux() * (long) ratio());
    }

    @Override
    public boolean isFull() {
        return this.cache.displayFlux() >= this.cache.displayFluxCapacity();
    }

    @Override
    public void receiveMana(int mana) {
        if (!Affinity.config().botaniaManaIntegration() || mana == 0) return;

        if (mana < 0) {
            // floor division, so draining can never round in the player's favour
            this.cache.updateFlux(Math.max(0, this.cache.flux() + Math.floorDiv(mana, ratio())));
            return;
        }

        // bursts are converted into whole flux, the remainder is simply dissipated
        final long flux = mana / ratio();
        if (flux <= 0) return;

        this.insertIntoCache(flux);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return Affinity.config().botaniaManaIntegration();
    }

    private void insertIntoCache(long flux) {
        // a stack of caches acts as a single buffer, so the burst is spread across all of
        // them -- sorted by height to keep it filling up bottom-up like the node network does
        final var world = this.cache.getWorld();
        final var members = this.cache.memberBlocks();
        final Iterable<AethumFluxCacheBlockEntity> targets = world == null || members.isEmpty()
                ? List.of(this.cache)
                : members.stream()
                        .sorted(Comparator.comparingInt(BlockPos::getY))
                        .map(world::getBlockEntity)
                        .filter(AethumFluxCacheBlockEntity.class::isInstance)
                        .map(AethumFluxCacheBlockEntity.class::cast)
                        .toList();

        for (var target : targets) {
            final long space = target.fluxCapacity() - target.flux();
            if (space <= 0) continue;

            final long inserted = Math.min(space, flux);
            target.updateFlux(target.flux() + inserted);
            flux -= inserted;

            if (flux <= 0) return;
        }
    }

}
