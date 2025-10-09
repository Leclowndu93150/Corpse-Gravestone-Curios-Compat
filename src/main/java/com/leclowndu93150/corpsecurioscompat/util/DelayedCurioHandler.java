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
    
    /**
     * Stores items to be processed after a delay for a specific player
     */
    public static void scheduleCurioRestoration(Player player, List<ItemStack> items) {
        if (items.isEmpty()) return;
        
        UUID playerId = player.getUUID();
        pendingItems.put(playerId, new ArrayList<>(items));
        
        long currentTime = System.currentTimeMillis();
        long executeTime = currentTime + 50; // 1 tick (50 ms) delay
        scheduledTimes.put(playerId, executeTime);
    }
    
    /**
     * Tick handler for a specific player
     */
    public static void tickForPlayer(Player player) {
        UUID playerId = player.getUUID();
        Long scheduledTime = scheduledTimes.get(playerId);
        
        if (scheduledTime == null) return;
        
        long currentTime = System.currentTimeMillis();
        
        if (currentTime >= scheduledTime) {
            processPendingItems(player);
            scheduledTimes.remove(playerId);
        }
    }
    
    /**
     * Processes pending items for a player
     */
    private static void processPendingItems(Player player) {
        UUID playerId = player.getUUID();
        List<ItemStack> items = pendingItems.remove(playerId);
        
        if (items == null || items.isEmpty()) return;
        
        var curiosOptional = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (!curiosOptional.isPresent()) return;
        
        var handler = curiosOptional.resolve().get();
        Map<String, ICurioStacksHandler> curios = handler.getCurios();

        for (ICurioStacksHandler stacksHandler : curios.values()) {
            if (stacksHandler instanceof ICuriosAccessor accessor) {
                accessor.corpsecurioscompat$forceSlotRebuild();
            }
        }
        
        for (ItemStack stack : items) {
            if (!tryEquipCurio(stack, player, curios)) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
    }
    
    /**
     * Tries to equip a curio item
     */
    private static boolean tryEquipCurio(ItemStack stack, Player player, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;
        
        CuriosSlotDataComponent.CurioSlotData slotData = CuriosSlotDataComponent.getCurioSlotData(stack);
        if (slotData == null) return false;
        
        String slotType = slotData.slotType();
        int slotIndex = slotData.slotIndex();
        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0) {
            var targetStacks = slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
            int availableSlots = targetStacks.getSlots();

            if (slotIndex < availableSlots) {
                ItemStack existingStack = targetStacks.getStackInSlot(slotIndex);
                
                if (existingStack.isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    CuriosSlotDataComponent.removeCurioSlotData(cleanStack);
                    targetStacks.setStackInSlot(slotIndex, cleanStack);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Cleanup pending items for a player (e.g., on logout)
     */
    public static void cleanupPlayer(Player player) {
        UUID playerId = player.getUUID();
        List<ItemStack> items = pendingItems.remove(playerId);
        scheduledTimes.remove(playerId);

        if (items != null && !items.isEmpty()) {
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