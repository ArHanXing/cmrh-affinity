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
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

        // summarize everything the core currently receives, so the player can
        // spot missing or mismatched inputs at a glance
        this.sendRitualMessage(player, Text.translatable("ritual.affinity.feedback.aspen.summary",
                inventory.primaryInput().getName().copy().formatted(Formatting.YELLOW),
                Text.literal(String.valueOf(socleStacks.size())).formatted(Formatting.YELLOW),
                Text.literal(formatStacks(socleStacks)).formatted(Formatting.YELLOW),
                Text.literal(String.valueOf(this.flux())).formatted(Formatting.YELLOW),
                Text.literal(String.valueOf(this.fluxCapacity())).formatted(Formatting.YELLOW)).formatted(Formatting.AQUA));

        // rank every recipe by how much of its input the current setup already
        // satisfies - the primary input is decisive, but socle matches are
        // counted individually so a recipe with (almost) all socle items present
        // reliably wins over one which merely shares the primary input
        var closest = recipes.get(0);
        int bestScore = Integer.MIN_VALUE;
        for (var entry : recipes) {
            final var recipe = entry.value();
            final var score = (recipe.primaryInput.test(inventory.primaryInput()) ? 8 : 0)
                    + partialMatchScore(recipe.socleInputs, socleStacks) * 2
                    - Math.abs(recipe.socleInputs.size() - socleStacks.size());

            if (score > bestScore) {
                bestScore = score;
                closest = entry;
            }
        }

        final var recipe = closest.value();

        final var missing = new ArrayList<Text>();
        if (!recipe.primaryInput.test(inventory.primaryInput())) {
            missing.add(Text.translatable("ritual.affinity.feedback.missing.primary", formatIngredient(recipe.primaryInput)).formatted(Formatting.YELLOW));
        }
        if (recipe.socleInputs.size() != socleStacks.size()) {
            missing.add(Text.translatable("ritual.affinity.feedback.missing.socle_count", recipe.socleInputs.size(), socleStacks.size()).formatted(Formatting.YELLOW));
        } else if (!recipe.doShapelessMatch(recipe.socleInputs, socleStacks)) {
            missing.add(Text.translatable("ritual.affinity.feedback.missing.socle_items", formatIngredients(recipe.socleInputs)).formatted(Formatting.YELLOW));
        }

        this.sendRitualMessage(player, Text.translatable("ritual.affinity.feedback.closest_recipe",
                Text.literal(closest.id().toString()).formatted(Formatting.LIGHT_PURPLE),
                Texts.join(missing, Text.literal("; "))));
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
