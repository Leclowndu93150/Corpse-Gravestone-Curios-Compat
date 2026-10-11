package com.leclowndu93150.corpsecurioscompat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
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

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");

        BLACKLISTED_ITEMS = COMMON_BUILDER
                .comment("Items that should not be transferred to curios slots (format: 'modid:item')")
                .defineList("blacklisted_items", List.of(), entry -> entry instanceof String);

        TRANSFER_CURSED_ITEMS = COMMON_BUILDER
                .comment("Whether cursed items (Curse of Binding) should be transferred back to curios slots")
                .define("transfer_cursed_items", false);

        COMMON_BUILDER.pop();
        COMMON = COMMON_BUILDER.build();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON);
    }

    public static boolean isItemBlacklisted(Item item) {
        return BLACKLISTED_ITEMS.get().contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean shouldTransferCursedItems() {
        return TRANSFER_CURSED_ITEMS.get();
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
