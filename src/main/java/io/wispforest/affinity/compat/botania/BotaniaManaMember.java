package io.wispforest.affinity.compat.botania;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.LinkableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.Optional;
import java.util.Set;

/**
 * Presents a Botania mana block as an ordinary member of an aethum flux network. Flux
 * inserted into it is converted into mana and pushed into the receiver, which allows nodes
 * to supply mana pools, spreaders, runic altars and every other block implementing
 * {@link ManaReceiver} exactly like they would supply an Affinity machine
 * <p>
 * A fresh instance is created for every lookup and links are only ever stored on the node
 * side, so nothing has to be persisted here. The per-tick transfer allowance is tracked on
 * the instance, which is the right granularity: the node keeps one instance per link and
 * probes it several times per tick before actually transferring
 */
public class BotaniaManaMember extends SnapshotParticipant<BotaniaManaMember.Snapshot> implements AethumNetworkMember, LinkableBlockEntity {

    private final World world;
    private final BlockPos pos;
    private final ManaReceiver receiver;

    private long budgetTick = -1;
    private long budgetSpent = 0;

    public BotaniaManaMember(World world, BlockPos pos, ManaReceiver receiver) {
        this.world = world;
        this.pos = pos.toImmutable();
        this.receiver = receiver;
    }

    public ManaReceiver receiver() {
        return this.receiver;
    }

    private static int ratio() {
        return Math.max(1, Affinity.config().botaniaManaPerFlux());
    }

    private static long transferRate() {
        return Affinity.config().botaniaFluxTransferPerTick();
    }

    // ------------
    // Flux storage
    // ------------

    @Override
    public long flux() {
        return this.receiver.getCurrentMana() / (long) ratio();
    }

    @Override
    public long fluxCapacity() {
        return this.receiver instanceof ManaPool pool ? pool.getMaxMana() / (long) ratio() : Long.MAX_VALUE;
    }

    @Override
    public void updateFlux(long flux) {
        this.setMana((int) Math.min(Integer.MAX_VALUE, Math.max(0, flux) * (long) ratio()));
    }

    // ---------
    // Insertion
    // ---------

    @Override
    public long insert(long max, TransactionContext transaction) {
        if (!this.canInsert()) return 0;

        final int ratio = ratio();
        final long flux = Math.min(max, this.availableBudget());
        final int space = this.space();

        if (flux <= 0 || space <= 0) return 0;

        final int mana = (int) Math.min(space, flux * ratio);
        if (mana <= 0) return 0;

        final int manaBefore = this.receiver.getCurrentMana();

        // the mana lands in a foreign block, so both it and the spent allowance have to be
        // rolled back by hand whenever the transaction is aborted -- which is exactly what
        // the node's potential-transfer probes do
        this.updateSnapshots(transaction);
        this.receiver.receiveMana(mana);

        final int accepted = this.receiver.getCurrentMana() - manaBefore;
        if (accepted <= 0) return 0;

        // only ever charge for the mana that actually landed, rounded up so that
        // converting back never generates free flux
        final long billed = Math.min(flux, (accepted + ratio - 1L) / ratio);
        this.spendBudget(billed);

        return billed;
    }

    @Override
    public boolean canInsert() {
        return transferRate() > 0 && !this.receiver.isFull();
    }

    @Override
    public long maxInsert() {
        return transferRate();
    }

    // ----------
    // Extraction
    // ----------

    @Override
    public long extract(long max, TransactionContext transaction) {
        if (!this.canExtract()) return 0;

        final int ratio = ratio();
        final long flux = Math.min(max, this.availableBudget());
        final int mana = (int) Math.min(this.receiver.getCurrentMana(), flux * ratio);

        if (mana <= 0) return 0;

        this.updateSnapshots(transaction);
        this.receiver.receiveMana(-mana);

        // floor, so that draining a pool can never create flux out of nothing
        final long billed = Math.min(flux, mana / ratio);
        this.spendBudget(billed);

        return billed;
    }

    @Override
    public boolean canExtract() {
        return Affinity.config().botaniaManaExtraction()
                && this.receiver instanceof ManaPool pool
                && pool.isOutputtingPower();
    }

    @Override
    public long maxExtract() {
        return this.canExtract() ? transferRate() : 0;
    }

    // -------------
    // Allowance
    // -------------

    private long availableBudget() {
        if (this.budgetTick != this.world.getTime()) return transferRate();

        return Math.max(0, transferRate() - this.budgetSpent);
    }

    private void spendBudget(long flux) {
        final long now = this.world.getTime();

        if (this.budgetTick != now) {
            this.budgetTick = now;
            this.budgetSpent = 0;
        }

        this.budgetSpent += flux;
    }

    // ------------
    // Transactions
    // ------------

    public record Snapshot(int mana, long budgetTick, long budgetSpent) {}

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(this.receiver.getCurrentMana(), this.budgetTick, this.budgetSpent);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.setMana(snapshot.mana());
        this.budgetTick = snapshot.budgetTick();
        this.budgetSpent = snapshot.budgetSpent();
    }

    private void setMana(int mana) {
        final int delta = mana - this.receiver.getCurrentMana();
        if (delta != 0) this.receiver.receiveMana(delta);
    }

    private int space() {
        if (this.receiver instanceof ManaPool pool) {
            return Math.max(0, pool.getMaxMana() - pool.getCurrentMana());
        }

        // receivers without an exposed capacity only tell us whether they are full --
        // anything beyond that is accounted for by measuring the accepted amount
        return this.receiver.isFull() ? 0 : Integer.MAX_VALUE / 4;
    }

    // -------
    // Linking
    // -------

    @Override
    public Set<BlockPos> linkedMembers() {
        return Set.of();
    }

    @Override
    public boolean isLinked(BlockPos pos) {
        return Affinity.AETHUM_NODE.find(this.world, pos, null) != null;
    }

    @Override
    public boolean acceptsLinks() {
        return true;
    }

    @Override
    public boolean addLinkParent(BlockPos pos, AethumLink.Type type) {
        return true;
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {}

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.NORMAL;
    }

    @Override
    public Optional<String> beginLink(PlayerEntity player, NbtCompound linkData) {
        linkData.put(AethumNetworkMemberBlockEntity.LINK_ELEMENT_KEY, AethumLink.Element.of(this));
        linkData.put(AethumNetworkMemberBlockEntity.LINK_TYPE_KEY, player.isSneaking() ? this.specialLinkType() : AethumLink.Type.NORMAL);

        return Optional.empty();
    }

    @Override
    public Optional<LinkResult> finishLink(PlayerEntity player, BlockPos linkTo, NbtCompound linkData) {
        if (linkData.get(AethumNetworkMemberBlockEntity.LINK_ELEMENT_KEY) != AethumLink.Element.NODE) return Optional.empty();

        final var node = Affinity.AETHUM_NODE.find(this.world, linkTo, null);
        if (node == null) return Optional.of(LinkResult.NO_TARGET);

        return Optional.of(node.createGenericLink(this.pos, linkData.get(AethumNetworkMemberBlockEntity.LINK_TYPE_KEY)));
    }

    @Override
    public Optional<LinkResult> destroyLink(PlayerEntity player, BlockPos destroyFrom, NbtCompound linkData) {
        if (linkData.get(AethumNetworkMemberBlockEntity.LINK_ELEMENT_KEY) != AethumLink.Element.NODE) return Optional.empty();

        final var node = Affinity.AETHUM_NODE.find(this.world, destroyFrom, null);
        if (node == null) return Optional.of(LinkResult.NO_TARGET);

        return Optional.of(node.destroyLink(this.pos));
    }

}
