package com.leclowndu93150.corpsecurioscompat;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("General settings").push("general");

        BLACKLISTED_ITEMS = COMMON_BUILDER
                .comment("Items that should not be transferred to curios slots (format: 'modid:item')")
                .defineList("blacklisted_items", List.of(), entry -> entry instanceof String);

        COMMON_BUILDER.pop();
        COMMON = COMMON_BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON);
    }

    public static boolean isItemBlacklisted(Item item) {
        return BLACKLISTED_ITEMS.get().contains(item.builtInRegistryHolder().key().location().toString());
    }
}