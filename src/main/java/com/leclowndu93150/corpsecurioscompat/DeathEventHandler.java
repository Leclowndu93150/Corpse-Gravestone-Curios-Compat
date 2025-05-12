package com.leclowndu93150.corpsecurioscompat;

import com.leclowndu93150.baguettelib.event.entity.death.PlayerDeathEvent;
import net.minecraft.nbt.CompoundTag;
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

    private static final String CURIO_SLOT_TAG = "CorpseCuriosSlot";
    private static final String CURIO_SLOT_TYPE = "SlotType";
    private static final String CURIO_SLOT_INDEX = "SlotIndex";
    private static final String WAS_EQUIPPED = "WasEquipped";

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

            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack stack = stackHandler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    tagItemWithSlotData(stack, slotType, i, true);
                }
            }

            for (int i = 0; i < stackHandler.getCosmeticStacks().getSlots(); i++) {
                ItemStack stack = stackHandler.getCosmeticStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    tagItemWithSlotData(stack, slotType, i, true);
                }
            }
        }
    }

    private static void tagItemWithSlotData(ItemStack stack, String slotType, int slotIndex, boolean wasEquipped) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag slotData = new CompoundTag();
        slotData.putString(CURIO_SLOT_TYPE, slotType);
        slotData.putInt(CURIO_SLOT_INDEX, slotIndex);
        slotData.putBoolean(WAS_EQUIPPED, wasEquipped);
        tag.put(CURIO_SLOT_TAG, slotData);
    }
}