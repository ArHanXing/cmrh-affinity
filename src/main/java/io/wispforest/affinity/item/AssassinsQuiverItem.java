package io.wispforest.affinity.item;

import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsAttributeModifiersComponent;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class AssassinsQuiverItem extends TrinketItem {

    public AssassinsQuiverItem() {
        super(AffinityItems.settings().maxCount(1).component(
                TrinketsAttributeModifiersComponent.TYPE,
                TrinketsAttributeModifiersComponent.builder()
                        .add(
                                Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.EXTRA_ARROW_DAMAGE),
                                new EntityAttributeModifier(Affinity.id("assassins_quiver_damage"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                "chest/back"
                        )
                        .build()
        ));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.affinity.assassins_quiver.tooltip.desc",
                Text.literal("3").formatted(Formatting.YELLOW)));
    }
}
