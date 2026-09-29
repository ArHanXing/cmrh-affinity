/*
 * Compile-time mirror of vazkii.botania.api.mana.ManaReceiver (Botania 1.21.1, Fabric).
 *
 * Verified against the published artifact with:
 *   javap -p vazkii/botania/api/mana/ManaReceiver.class
 * which yields exactly the two static fields and six abstract methods below.
 *
 * This file is NOT part of the mod: it lives in the compile-only `botaniaApi`
 * source set, is never bundled into the jar and is never loaded at runtime.
 */
package vazkii.botania.api.mana;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Any block whose block entity implements this can hold mana and receive it from mana bursts.
 */
public interface ManaReceiver {

    Identifier ID = Identifier.of("botania", "mana_receiver");

    World getManaReceiverLevel();

    BlockPos getManaReceiverPos();

    /**
     * @return the amount of mana currently stored in this block
     */
    int getCurrentMana();

    /**
     * @return whether this receiver is full, meaning no further mana bursts will be sent to it
     */
    boolean isFull();

    /**
     * Called when this receiver is given mana. May be negative to drain.
     */
    void receiveMana(int mana);

    /**
     * @return whether this receiver accepts mana from bursts
     */
    boolean canReceiveManaFromBursts();

}
