package com.leclowndu93150.corpsecurioscompat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    public static final ForgeConfigSpec.BooleanValue TRANSFER_CURSED_ITEMS;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
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

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON);
    }

    public static boolean isItemBlacklisted(Item item) {
        return BLACKLISTED_ITEMS.get().contains(item.builtInRegistryHolder().key().location().toString());
    }

    public static boolean shouldTransferCursedItems() {
        return TRANSFER_CURSED_ITEMS.get();
    }

    public static boolean isItemCursed(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) > 0;
    }
}
