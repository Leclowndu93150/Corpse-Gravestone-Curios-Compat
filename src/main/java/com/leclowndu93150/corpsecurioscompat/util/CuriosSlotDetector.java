package com.leclowndu93150.corpsecurioscompat.util;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import com.leclowndu93150.corpsecurioscompat.Main;

public class CuriosSlotDetector {
    
    /**
     * Detects if an item would add slot modifiers when equipped.
     * This checks if the item has any SlotAttribute modifiers that would increase slot counts.
     */
    public static boolean doesItemAddSlots(ItemStack stack, Player player, String slotType) {
        if (stack.isEmpty()) return false;

        var curiosOpt = CuriosApi.getCurio(stack);
        if (curiosOpt.isEmpty()) return false;
        
        ICurio curio = curiosOpt.get();

        SlotContext slotContext = new SlotContext(slotType, player, 0, false, true);

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Main.MODID, "temp_check");
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = curio.getAttributeModifiers(slotContext, id);

        for (var entry : modifiers.entries()) {
            Attribute attr = entry.getKey().value();
            AttributeModifier modifier = entry.getValue();

            if (attr instanceof SlotAttribute slotAttr) {
                if (modifier.amount() > 0 && 
                    modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    return true;
                }
            }
        }
        

        Multimap<Holder<Attribute>, AttributeModifier> stackModifiers = CuriosApi.getAttributeModifiers(slotContext, id, stack);
        
        for (var entry : stackModifiers.entries()) {
            Attribute attr = entry.getKey().value();
            AttributeModifier modifier = entry.getValue();
            
            if (attr instanceof SlotAttribute) {
                if (modifier.amount() > 0 && 
                    modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    return true;
                }
            }
        }
        
        return false;
    }
}