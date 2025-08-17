package com.leclowndu93150.corpsecurioscompat.event;

import com.leclowndu93150.baguettelib.event.entity.death.PlayerDeathEvent;
import com.leclowndu93150.corpsecurioscompat.data.CuriosSlotDataComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "corpsecurioscompat")
public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeathPre(PlayerDeathEvent.Pre event) {
        Player player = event.getPlayer();

        var curioHandlerOptional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (!curioHandlerOptional.isPresent()) {
            return;
        }

        ICuriosItemHandler handler = curioHandlerOptional.resolve().get();
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            String slotType = entry.getKey();
            ICurioStacksHandler stackHandler = entry.getValue();

            // Tag regular curio items
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    CuriosSlotDataComponent.setCurioSlotData(stack, slotType, i, true, false);
                }
            }

            // Tag cosmetic curio items
            for (int i = 0; i < stackHandler.getCosmeticStacks().getSlots(); i++) {
                ItemStack stack = stackHandler.getCosmeticStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    CuriosSlotDataComponent.setCurioSlotData(stack, slotType, i, true, true);
                }
            }
        }
    }
}