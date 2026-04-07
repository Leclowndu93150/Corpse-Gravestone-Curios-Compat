package com.leclowndu93150.accessoriescompat.event;

import com.leclowndu93150.accessoriescompat.data.AccessorySlotDataComponent;
import com.leclowndu93150.baguettelib.event.entity.death.PlayerDeathEvent;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "accessoriescorpsecompat")
public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeathPre(PlayerDeathEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return;

        for (var entry : cap.getContainers().entrySet()) {
            String slotName = entry.getKey();
            AccessoriesContainer container = entry.getValue();

            for (int i = 0; i < container.getSize(); i++) {
                ItemStack stack = container.getAccessories().getItem(i);
                if (!stack.isEmpty()) {
                    AccessorySlotDataComponent.setSlotData(stack, slotName, i);
                }
            }
        }
    }
}
