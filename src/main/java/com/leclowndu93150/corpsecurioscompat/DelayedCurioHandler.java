package com.leclowndu93150.corpsecurioscompat;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;

public class DelayedCurioHandler {
    private static final Map<UUID, List<ItemStack>> pendingItems = new HashMap<>();
    
    /**
     * Stores items to be processed after a delay for a specific player
     */
    public static void scheduleCurioRestoration(Player player, List<ItemStack> items) {
        if (items.isEmpty()) return;
        
        UUID playerId = player.getUUID();
        pendingItems.put(playerId, new ArrayList<>(items));

        MinecraftServer server = player.level().getServer();

        // Wait 5 ticks to ensure Curios has fully processed slot additions
        server.tell(new TickTask(server.getTickCount() + 5,
            () -> processPendingItems(player)
        ));
    }
    
    /**
     * Processes pending items for a player
     */
    private static void processPendingItems(Player player) {
        UUID playerId = player.getUUID();
        List<ItemStack> items = pendingItems.remove(playerId);
        
        if (items == null || items.isEmpty()) return;
        
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
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
        });
    }
    
    /**
     * Tries to equip a curio item
     */
    private static boolean tryEquipCurio(ItemStack stack, Player player, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;
        
        CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
        if (slotData == null) return false;
        
        String slotType = slotData.slotType();
        int slotIndex = slotData.slotIndex();

        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0) {
            var targetStacks = slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
            
            if (slotIndex < targetStacks.getSlots()) {
                ItemStack existingStack = targetStacks.getStackInSlot(slotIndex);
                
                if (existingStack.isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                    targetStacks.setStackInSlot(slotIndex, cleanStack);
                    return true;
                }
            }
        }

        return tryFindAlternativeSlot(stack, curios);
    }
    
    private static boolean tryFindAlternativeSlot(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
        if (slotData == null) return false;
        
        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            if (!CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(entry.getKey())) {
                continue;
            }
            
            ICurioStacksHandler handler = entry.getValue();
            // Try preferred slots first (cosmetic if item was cosmetic, regular otherwise)
            for (int pass = 0; pass < 2; pass++) {
                var stacks = (pass == 0) ? 
                    (slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks()) :
                    (slotData.isCosmetic() ? handler.getStacks() : handler.getCosmeticStacks());
                    
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    try {
                        if (stacks.getStackInSlot(slot).isEmpty()) {
                            ItemStack cleanStack = stack.copy();
                            cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                            stacks.setStackInSlot(slot, cleanStack);
                            return true;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Ignore and continue
                    }
                }
            }
        }
        
        return false;
    }
}