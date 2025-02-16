package com.leclowndu93150.corpsecurioscompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    public static final ModConfigSpec COMMON;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");
        BLACKLISTED_ITEMS = COMMON_BUILDER
                .comment("Items that should not be transferred to curios slots (format: 'modid:item')")
                .defineList("blacklisted_items", List.of(), entry -> entry instanceof String);
        COMMON_BUILDER.pop();
        COMMON = COMMON_BUILDER.build();
    }
    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON,COMMON);
    }
    public static boolean isItemBlacklisted(Item item) {
        return BLACKLISTED_ITEMS.get().contains(BuiltInRegistries.ITEM.getKey(item));
    }
}