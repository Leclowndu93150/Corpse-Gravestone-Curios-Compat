package com.leclowndu93150.corpsecurioscompat.event;

import com.leclowndu93150.corpsecurioscompat.util.DelayedCurioHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "gravestonecurioscompat")
public class TickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            DelayedCurioHandler.tickForPlayer(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DelayedCurioHandler.cleanupPlayer(event.getEntity());
    }
}
