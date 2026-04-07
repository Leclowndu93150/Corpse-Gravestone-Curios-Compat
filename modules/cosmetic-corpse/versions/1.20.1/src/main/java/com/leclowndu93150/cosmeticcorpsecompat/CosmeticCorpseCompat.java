package com.leclowndu93150.cosmeticcorpsecompat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("cosmeticcorpsecompat")
public class CosmeticCorpseCompat {

    private static final int USELESS = 0;

    public CosmeticCorpseCompat() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CosmeticArmorDeathEventHandler.init();
    }
}