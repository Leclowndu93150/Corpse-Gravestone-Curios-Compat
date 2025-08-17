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
    
    /**
     * Detects if an item would add slot modifiers when equipped.
     * This checks if the item has any SlotAttribute modifiers that would increase slot counts.
     */
    public static boolean doesItemAddSlots(ItemStack stack, Player player, String slotType) {
        if (stack.isEmpty()) return false;

        var curiosOpt = CuriosApi.getCurio(stack);
        if (curiosOpt.resolve().isEmpty()) return false;
        
        ICurio curio = curiosOpt.resolve().get();

        SlotContext slotContext = new SlotContext(slotType, player, 0, false, true);

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        Multimap<Attribute, AttributeModifier> modifiers = curio.getAttributeModifiers(slotContext, uuid);

        for (var entry : modifiers.entries()) {
            Attribute attr = entry.getKey();
            AttributeModifier modifier = entry.getValue();

            if (attr instanceof SlotAttribute slotAttr) {
                if (modifier.getAmount() > 0 && 
                    modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    return true;
                }
            }
        }
        
        Multimap<Attribute, AttributeModifier> stackModifiers = CuriosApi.getAttributeModifiers(slotContext, uuid, stack);
        
        for (var entry : stackModifiers.entries()) {
            Attribute attr = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            
            if (attr instanceof SlotAttribute) {
                if (modifier.getAmount() > 0 && 
                    modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    return true;
                }
            }
        }
        
        return false;
    }
}