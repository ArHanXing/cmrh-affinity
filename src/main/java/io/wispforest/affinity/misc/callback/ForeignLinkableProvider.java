package io.wispforest.affinity.misc.callback;

import io.wispforest.affinity.blockentity.template.LinkableBlockEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Allows optional integrations to expose blocks which cannot implement
 * {@link LinkableBlockEntity} themselves (because they belong to another mod)
 * as valid endpoints for the wand of iridescence
 */
public interface ForeignLinkableProvider {

    Event<ForeignLinkableProvider> EVENT = EventFactory.createArrayBacked(ForeignLinkableProvider.class, providers -> (world, pos, blockEntity) -> {
        for (var provider : providers) {
            var linkable = provider.findLinkable(world, pos, blockEntity);
            if (linkable != null) return linkable;
        }

        return null;
    });

    /**
     * @return A {@link LinkableBlockEntity} representing the block at {@code pos},
     * or {@code null} if this provider does not handle it
     */
    @Nullable
    LinkableBlockEntity findLinkable(World world, BlockPos pos, @Nullable BlockEntity blockEntity);

}
