package com.leclowndu93150.accessoriescompat.event;

import com.leclowndu93150.accessoriescompat.util.DelayedAccessoryHandler;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "accessoriescorpsecompat")
public class TickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            DelayedAccessoryHandler.tickForPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DelayedAccessoryHandler.cleanupPlayer(event.getEntity());
    }
}
