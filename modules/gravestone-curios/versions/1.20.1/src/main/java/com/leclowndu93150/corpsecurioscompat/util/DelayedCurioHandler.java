package com.leclowndu93150.corpsecurioscompat.util;

import com.leclowndu93150.corpsecurioscompat.data.CuriosSlotDataComponent;
import com.leclowndu93150.corpsecurioscompat.duck.ICuriosAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;

public class DelayedCurioHandler {
    private static final Map<UUID, List<ItemStack>> pendingItems = new HashMap<>();
    private static final Map<UUID, Long> scheduledTimes = new HashMap<>();

    public static void scheduleCurioRestoration(Player player, List<ItemStack> items) {
        if (items.isEmpty()) return;
        pendingItems.put(player.getUUID(), new ArrayList<>(items));
        scheduledTimes.put(player.getUUID(), System.currentTimeMillis() + 50);
    }

    public static void tickForPlayer(Player player) {
        Long scheduledTime = scheduledTimes.get(player.getUUID());
        if (scheduledTime == null) return;
        if (System.currentTimeMillis() >= scheduledTime) {
            processPendingItems(player);
            scheduledTimes.remove(player.getUUID());
        }
    }

    private static void processPendingItems(Player player) {
        List<ItemStack> items = pendingItems.remove(player.getUUID());
        if (items == null || items.isEmpty()) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (ICurioStacksHandler stacksHandler : curios.values()) {
                if (stacksHandler instanceof ICuriosAccessor accessor) {
                    accessor.gravestonecurioscompat$forceSlotRebuild();
                }
            }

            for (ItemStack stack : items) {
                if (!tryEquipCurio(stack, player, curios)) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }
        });
    }

    private static boolean tryEquipCurio(ItemStack stack, Player player, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;

        CuriosSlotDataComponent.CurioSlotData slotData = CuriosSlotDataComponent.getCurioSlotData(stack);
        if (slotData == null) return false;

        String slotType = slotData.slotType();
        int slotIndex = slotData.slotIndex();
        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0) {
            var targetStacks = slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
            if (slotIndex < targetStacks.getSlots()) {
                if (targetStacks.getStackInSlot(slotIndex).isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    CuriosSlotDataComponent.removeCurioSlotData(cleanStack);
                    targetStacks.setStackInSlot(slotIndex, cleanStack);
                    return true;
                }
            }
        }
        return false;
    }

    public static void cleanupPlayer(Player player) {
        List<ItemStack> items = pendingItems.remove(player.getUUID());
        scheduledTimes.remove(player.getUUID());
        if (items != null) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    CuriosSlotDataComponent.removeCurioSlotData(cleanStack);
                    if (!player.getInventory().add(cleanStack)) {
                        player.drop(cleanStack, false);
                    }
                }
            }
        }
    }
}
