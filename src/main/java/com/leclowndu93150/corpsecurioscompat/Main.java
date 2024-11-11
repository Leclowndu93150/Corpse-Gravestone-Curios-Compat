package com.leclowndu93150.corpsecurioscompat;

import de.maxhenkel.corpse.corelib.death.PlayerDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Main.MODID)
public class Main
{
    public static final String MODID = "corpsecurioscompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(new Balls());
    }

    public class Balls {

        public Balls() {
            de.maxhenkel.corpse.corelib.death.DeathEvents.register();
        }

        @SubscribeEvent
        public void onDeath(PlayerDeathEvent event) {
            LOGGER.info(String.valueOf(event.getDeath().getAdditionalItems()));
        }
    }
}
