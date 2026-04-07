package com.leclowndu93150.corpsecurioscompat;

import com.leclowndu93150.corpsecurioscompat.data.CuriosSlotDataComponent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Main.MODID)
public class Main
{
    private static final int USELESS = 0;
    public static final String MODID = "corpsecurioscompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modContainer);
        CuriosSlotDataComponent.DATA_COMPONENTS.register(modEventBus);
        LOGGER.info("Hello from Corpse Curios Compat!");
    }

}
