package com.leclowndu93150.accessoriescompat.event;

import com.leclowndu93150.accessoriescompat.util.DelayedAccessoryHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "accessoriescorpsecompat")
public class TickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            DelayedAccessoryHandler.tickForPlayer(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DelayedAccessoryHandler.cleanupPlayer(event.getEntity());
    }
}
