package com.leclowndu93150.cosmeticcorpsecompat;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Cosmeticcorpsecompat.MODID)
public class Cosmeticcorpsecompat {
    public static final String MODID = "cosmeticcorpsecompat";

    public Cosmeticcorpsecompat(IEventBus modEventBus, ModContainer modContainer) {
        CosmeticArmorDeathEventHandler.COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CosmeticArmorDeathEventHandler.init();
    }

}
