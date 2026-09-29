/*
 * Compile-time mirror of vazkii.botania.api.mana.ManaPool (Botania 1.21.1, Fabric).
 *
 * Verified against the published artifact with:
 *   javap -p vazkii/botania/api/mana/ManaPool.class
 *
 * See ManaReceiver.java in this source set for why this mirror exists.
 */
package vazkii.botania.api.mana;

/**
 * Any {@link ManaReceiver} that also implements this is considered a mana pool, from which
 * nearby functional flowers will pull mana.
 */
public interface ManaPool extends ManaReceiver {

    /**
     * @return false while the pool accepts mana from items, true while it pushes mana into them
     */
    boolean isOutputtingPower();

    /**
     * @return the maximum amount of storable mana
     */
    int getMaxMana();

}
