package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.endec.Endec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class AttunedShardItem extends Item {

    public static final ComponentType<Float> HEALTH = Affinity.component("attuned_shard_health", Endec.FLOAT);

    private final AttunedShardTier tier;

    public AttunedShardItem(Settings settings, AttunedShardTier tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.affinity.attuned_shard.tooltip.transfer",
                Text.literal(String.valueOf(this.tier.maxTransfer())).formatted(Formatting.YELLOW)));
        tooltip.add(Text.translatable("item.affinity.attuned_shard.tooltip.range",
                Text.literal(String.valueOf(this.tier.maxDistance())).formatted(Formatting.AQUA)));
    }

    public AttunedShardTier tier() {
        return tier;
    }

    public static void damageShard(ItemStack shard, float damage) {
        shard.set(HEALTH, MathHelper.clamp(getShardHealth(shard) - damage, 0f, 1f));
    }

    public static float getShardHealth(ItemStack shard) {
        return shard.getOrDefault(HEALTH, 1f);
    }
}
