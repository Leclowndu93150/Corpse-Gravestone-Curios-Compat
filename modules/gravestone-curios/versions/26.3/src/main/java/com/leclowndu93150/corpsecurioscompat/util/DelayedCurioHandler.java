package com.leclowndu93150.corpsecurioscompat.util;

import com.leclowndu93150.baguettelib.curios.BaguetteCuriosData;
import com.leclowndu93150.baguettelib.curios.CurioSlotData;
import com.leclowndu93150.baguettelib.curios.CuriosUtils;
import com.leclowndu93150.corpsecurioscompat.Config;
import com.leclowndu93150.corpsecurioscompat.duck.ICuriosAccessor;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

        CuriosUtils.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (ICurioStacksHandler stacksHandler : curios.values()) {
                if (stacksHandler instanceof ICuriosAccessor accessor) {
                    accessor.gravestonecurioscompat$forceSlotRebuild();
                }
            }

            for (ItemStack stack : items) {
                if (!tryEquipCurio(stack, player, curios)) {
                    ItemStack cleanStack = stack.copy();
                    BaguetteCuriosData.removeSlotData(cleanStack);
                    giveOrDrop(player, cleanStack);
                }
            }
        });
    }

    private static boolean tryEquipCurio(ItemStack stack, Player player, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;
        CurioSlotData slotData = BaguetteCuriosData.getSlotData(stack);
        if (slotData == null) return false;

        ICurioStacksHandler handler = curios.get(slotData.slotType());
        if (handler == null || slotData.slotIndex() < 0) return false;

        var targetStacks = slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
        if (slotData.slotIndex() >= targetStacks.getSlots()) return false;

        ItemStack existing = targetStacks.getStackInSlot(slotData.slotIndex());
        if (!existing.isEmpty() && !Config.shouldSwapOut(existing)) return false;

        ItemStack cleanStack = stack.copy();
        BaguetteCuriosData.removeSlotData(cleanStack);
        targetStacks.setStackInSlot(slotData.slotIndex(), cleanStack);
        if (!existing.isEmpty()) {
            giveOrDrop(player, existing);
        }
        return true;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }

    public static void cleanupPlayer(Player player) {
        List<ItemStack> items = pendingItems.remove(player.getUUID());
        scheduledTimes.remove(player.getUUID());
        if (items != null) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    BaguetteCuriosData.removeSlotData(cleanStack);
                    giveOrDrop(player, cleanStack);
                }
            }
        }
    }
}
