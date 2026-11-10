package com.leclowndu93150.corpsecurioscompat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    public static final ModConfigSpec COMMON;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    public static final ModConfigSpec.BooleanValue TRANSFER_CURSED_ITEMS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_SLOTS;
    public static final ModConfigSpec.BooleanValue SWAP_OCCUPIED_SLOTS;

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");

        BLACKLISTED_ITEMS = COMMON_BUILDER
                .comment("Items that should not be transferred to curios slots (format: 'modid:item' or '#modid:tag')")
                .defineList("blacklisted_items", List.of(), entry -> entry instanceof String);

        TRANSFER_CURSED_ITEMS = COMMON_BUILDER
                .comment("Whether cursed items should be transferred back to curios slots")
                .define("transfer_cursed_items", false);

        BLACKLISTED_SLOTS = COMMON_BUILDER
                .comment("Curio slot types that items should never be restored into (e.g. 'ring', 'back')")
                .defineList("blacklisted_slots", List.of(), entry -> entry instanceof String);

        SWAP_OCCUPIED_SLOTS = COMMON_BUILDER
                .comment("If the original curio slot is already occupied, put the restored item back into it and move the current item to the inventory (items with Curse of Binding are never swapped out)")
                .define("swap_occupied_slots", false);

        COMMON_BUILDER.pop();
        COMMON = COMMON_BUILDER.build();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON);
    }

    public static boolean isItemBlacklisted(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (String entry : BLACKLISTED_ITEMS.get()) {
            if (entry.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(entry.substring(1));
                if (tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId))) return true;
            } else if (entry.equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSlotBlacklisted(String slotType) {
        return BLACKLISTED_SLOTS.get().contains(slotType);
    }

    public static boolean shouldTransferCursedItems() {
        return TRANSFER_CURSED_ITEMS.get();
    }

    public static boolean shouldSwapOut(ItemStack equipped) {
        return SWAP_OCCUPIED_SLOTS.get() && !isItemCursed(equipped);
    }

    public static boolean isItemCursed(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(Enchantments.BINDING_CURSE) && entry.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}
