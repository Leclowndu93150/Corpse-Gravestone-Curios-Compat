package com.leclowndu93150.accessoriescompat.util;

import com.leclowndu93150.accessoriescompat.data.AccessorySlotDataComponent;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class DelayedAccessoryHandler {
    private static final Map<UUID, List<ItemStack>> pendingItems = new HashMap<>();
    private static final Map<UUID, Long> scheduledTimes = new HashMap<>();

    public static void scheduleRestoration(Player player, List<ItemStack> items) {
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

        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) {
            for (ItemStack stack : items) {
                ItemStack clean = stack.copy();
                AccessorySlotDataComponent.removeSlotData(clean);
                if (!player.getInventory().add(clean)) {
                    player.drop(clean, false);
                }
            }
            return;
        }

        for (ItemStack stack : items) {
            if (!tryEquipAccessory(stack, player, cap)) {
                ItemStack clean = stack.copy();
                AccessorySlotDataComponent.removeSlotData(clean);
                if (!player.getInventory().add(clean)) {
                    player.drop(clean, false);
                }
            }
        }
    }

    private static boolean tryEquipAccessory(ItemStack stack, Player player, AccessoriesCapability cap) {
        if (stack.isEmpty()) return false;

        AccessorySlotDataComponent.AccessorySlotData slotData = AccessorySlotDataComponent.getSlotData(stack);
        if (slotData == null) return false;

        AccessoriesContainer container = cap.getContainers().get(slotData.slotName());
        if (container != null && slotData.slotIndex() >= 0 && slotData.slotIndex() < container.getSize()) {
            ItemStack existing = container.getAccessories().getItem(slotData.slotIndex());
            if (existing.isEmpty()) {
                ItemStack clean = stack.copy();
                AccessorySlotDataComponent.removeSlotData(clean);
                container.getAccessories().setItem(slotData.slotIndex(), clean);
                return true;
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
                    ItemStack clean = stack.copy();
                    AccessorySlotDataComponent.removeSlotData(clean);
                    if (!player.getInventory().add(clean)) {
                        player.drop(clean, false);
                    }
                }
            }
        }
    }
}
