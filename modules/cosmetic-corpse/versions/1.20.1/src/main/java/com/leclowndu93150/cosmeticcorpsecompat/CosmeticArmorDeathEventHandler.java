package com.leclowndu93150.cosmeticcorpsecompat;

import lain.mods.cos.api.event.CosArmorDeathDrops;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CosmeticArmorDeathEventHandler {
    public static final String COSMETIC_ARMOR_TAG = "CosmeticArmorItem";
    public static final String SKIN_ARMOR_TAG = "CosmeticSkinArmor";
    public static final String SLOT_INDEX_TAG = "CosmeticSlotIndex";

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new CosmeticArmorDeathEventHandler());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCosmeticArmorDeath(CosArmorDeathDrops event) {

        CAStacksBase cosArmorInventory = event.getCAStacks();

        for (int i = 0; i < cosArmorInventory.getSlots(); i++) {
            ItemStack stack = cosArmorInventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(COSMETIC_ARMOR_TAG, true);
            tag.putInt(SLOT_INDEX_TAG, i);
            tag.putBoolean(SKIN_ARMOR_TAG, cosArmorInventory.isSkinArmor(i));
        }

    }
}