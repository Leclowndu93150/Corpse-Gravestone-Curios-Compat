package com.leclowndu93150.corpsecurioscompat;

import com.leclowndu93150.corpsecurioscompat.event.DeathEventHandler;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "gravestonecurioscompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modContainer);
        DeathEventHandler.register();
        LOGGER.info("Hello from Gravestone Curios Compat!");
    }
}
