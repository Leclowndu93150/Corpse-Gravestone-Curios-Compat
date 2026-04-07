package com.leclowndu93150.corpsecurioscompat.event;

import com.leclowndu93150.baguettelib.curios.BaguetteCuriosData;
import com.leclowndu93150.baguettelib.curios.CuriosUtils;
import com.leclowndu93150.baguettelib.event.entity.death.PlayerDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.common.CuriosConfig;

@EventBusSubscriber(modid = "gravestonecurioscompat")
public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeathPre(PlayerDeathEvent.Pre event) {
        Player player = event.getPlayer();
        if (areCuriosKept(player)) return;

        CuriosUtils.forEachEquipped(player, (ctx, stack) ->
            BaguetteCuriosData.setSlotData(stack, ctx.identifier(), ctx.index(), true, false)
        );

        CuriosUtils.forEachCosmetic(player, (ctx, stack) ->
            BaguetteCuriosData.setSlotData(stack, ctx.identifier(), ctx.index(), true, true)
        );
    }

    private static boolean areCuriosKept(Player player) {
        CuriosConfig.KeepCurios setting = CuriosConfig.SERVER.keepCurios.get();
        if (setting == CuriosConfig.KeepCurios.ON) return true;
        if (setting == CuriosConfig.KeepCurios.OFF) return false;
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }
}
