package com.leclowndu93150.cosmeticcorpsecompat;

import com.mojang.serialization.Codec;
import lain.mods.cos.api.event.CosArmorDeathDrops;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CosmeticArmorDeathEventHandler {
    private static final String MODID = "cosmeticcorpsecompat";

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final Supplier<DataComponentType<Boolean>> COSMETIC_ARMOR_COMPONENT = COMPONENTS.register("cosmetic_armor_item",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<DataComponentType<Boolean>> SKIN_ARMOR_COMPONENT = COMPONENTS.register("cosmetic_skin_armor",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<DataComponentType<Integer>> SLOT_INDEX_COMPONENT = COMPONENTS.register("cosmetic_slot_index",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static void init() {
        NeoForge.EVENT_BUS.register(new CosmeticArmorDeathEventHandler());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCosmeticArmorDeath(CosArmorDeathDrops event) {
        CAStacksBase cosArmorInventory = event.getCAStacks();

        for (int i = 0; i < cosArmorInventory.getSlots(); i++) {
            ItemStack stack = cosArmorInventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            stack.set(COSMETIC_ARMOR_COMPONENT.get(), true);
            stack.set(SLOT_INDEX_COMPONENT.get(), i);
            stack.set(SKIN_ARMOR_COMPONENT.get(), cosArmorInventory.isSkinArmor(i));
        }
    }
}