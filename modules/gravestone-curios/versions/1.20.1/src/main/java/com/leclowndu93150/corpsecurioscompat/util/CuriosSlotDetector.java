package com.leclowndu93150.corpsecurioscompat.util;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.UUID;

public class CuriosSlotDetector {

    private static final UUID TEMP_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public static boolean doesItemAddSlots(ItemStack stack, Player player, String slotType) {
        if (stack.isEmpty()) return false;

        var curiosOpt = CuriosApi.getCurio(stack);
        if (!curiosOpt.isPresent()) return false;

        ICurio curio = curiosOpt.resolve().get();
        SlotContext slotContext = new SlotContext(slotType, player, 0, false, true);
        Multimap<Attribute, AttributeModifier> modifiers = curio.getAttributeModifiers(slotContext, TEMP_UUID);

        for (var entry : modifiers.entries()) {
            Attribute attr = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            if (attr instanceof SlotAttribute) {
                if (modifier.getAmount() > 0 && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    return true;
                }
            }
        }

        return false;
    }
}
