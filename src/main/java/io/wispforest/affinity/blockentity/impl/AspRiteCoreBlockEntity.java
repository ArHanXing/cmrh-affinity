package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.affinity.recipe.AspenInfusionRecipe;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AspRiteCoreBlockEntity extends RitualCoreBlockEntity {

    @Nullable private AspenInfusionRecipe cachedRecipe = null;
    @Nullable private ItemStack cachedResult = null;

    public AspRiteCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASP_RITE_CORE, pos, state);

        this.fluxStorage.setFluxCapacity(16000);
        this.fluxStorage.setMaxInsert(200);
    }

    @Override
    protected boolean onRitualStart(RitualSetup setup, PlayerEntity player) {
        final var inventory = new AspenInfusionRecipeInput(setup.resolveSocles(this.world), this.item);
        final var recipeOptional = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASPEN_INFUSION, inventory, this.world);

        if (recipeOptional.isEmpty()) {
            this.diagnoseFailedRecipe(player, inventory);
            return false;
        }
        this.cachedRecipe = recipeOptional.get().value();
        this.cachedResult = this.cachedRecipe.craft(inventory, this.world.getRegistryManager());

        setup.configureLength(this.cachedRecipe.duration);

        return true;
    }

    private void diagnoseFailedRecipe(PlayerEntity player, AspenInfusionRecipeInput inventory) {
        final var socleStacks = inventory.delegate().stream().filter(stack -> !stack.isEmpty()).toList();
        final var recipes = this.world.getRecipeManager().listAllOfType(AffinityRecipeTypes.ASPEN_INFUSION);

        // the core item does not match the primary input of any recipe
        final var primaryMatching = recipes.stream()
                .filter(entry -> entry.value().primaryInput.test(inventory.primaryInput()))
                .toList();

        if (primaryMatching.isEmpty()) {
            this.sendRitualMessage(player, Text.translatable("ritual.affinity.feedback.aspen.primary_input_invalid",
                    inventory.primaryInput().getName()));
            return;
        }

        // the core item matches, but the number of socle items is wrong
        final var requiredSocleCount = primaryMatching.stream()
                .mapToInt(entry -> entry.value().socleInputs.size())
                .findFirst().orElse(-1);

        if (socleStacks.size() != requiredSocleCount) {
            this.sendRitualMessage(player, Text.translatable("ritual.affinity.feedback.socle_count_mismatch",
                    requiredSocleCount, socleStacks.size()));
            return;
        }

        // the core item and socle item count match, but the items themselves do not
        final var exampleRecipe = primaryMatching.stream()
                .filter(entry -> entry.value().socleInputs.size() == socleStacks.size())
                .findFirst().orElse(null);

        if (exampleRecipe != null) {
            final var requiredItems = exampleRecipe.value().socleInputs.stream()
                    .map(ingredient -> Arrays.stream(ingredient.getMatchingStacks()).findFirst())
                    .filter(Optional::isPresent)
                    .map(optional -> optional.get().getName().getString())
                    .distinct()
                    .collect(Collectors.joining(", "));

            this.sendRitualMessage(player, Text.translatable("ritual.affinity.feedback.aspen.socle_mismatch", requiredItems));
        }
    }

    @Override
    protected void doRitualTick() {
        if (this.cachedRecipe.fluxCostPerTick > 0) {
            if (this.flux() < this.cachedRecipe.fluxCostPerTick) {
                this.endRitual(this::onRitualInterrupted, false);
                return;
            } else {
                this.updateFlux(this.flux() - this.cachedRecipe.fluxCostPerTick);
            }
        }

        if (this.ritualTick % 10 == 0) {
            AffinityParticleSystems.ASPEN_INFUSION_ACTIVE.spawn(this.world, Vec3d.ofCenter(this.pos, .85f));
        }

        if (this.ritualTick % 80 == 0) {
            WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_ASP_RITE_CORE_ACTIVE, SoundCategory.BLOCKS);
        }
    }

    @Override
    protected boolean onRitualCompleted() {
        this.item = this.cachedResult;

        this.cachedRecipe = null;
        this.cachedResult = null;

        AffinityParticleSystems.ASPEN_INFUSION_CRAFT.spawn(this.world, Vec3d.ofCenter(this.pos, 1f));
        WorldOps.playSound(this.world, this.pos, AffinitySoundEvents.BLOCK_ASP_RITE_CORE_CRAFT, SoundCategory.BLOCKS);

        return true;
    }

    @Override
    protected boolean onRitualInterrupted() {
        AffinityParticleSystems.ASPEN_INFUSION_FAILS.spawn(this.world, Vec3d.ofCenter(this.pos, .85), this.item);
        return false;
    }

    @Override
    protected Vec3d modulatorStreamTargetPos() {
        return Vec3d.ofCenter(this.pos, .85);
    }

    public static class AspenInfusionRecipeInput extends SocleRecipeInput {

        private final ItemStack primaryInput;

        public AspenInfusionRecipeInput(List<RitualSocleBlockEntity> socles, ItemStack primaryInput) {
            super(socles);
            this.primaryInput = primaryInput.copy();
        }

        public ItemStack primaryInput() {
            return primaryInput;
        }
    }
}
