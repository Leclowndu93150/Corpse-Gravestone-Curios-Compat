package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.corpsecurioscompat.Config;
import com.leclowndu93150.corpsecurioscompat.data.CuriosSlotDataComponent;
import com.leclowndu93150.corpsecurioscompat.util.CuriosSlotDetector;
import com.leclowndu93150.corpsecurioscompat.util.DelayedCurioHandler;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = {CorpseInventoryContainer.class, CorpseAdditionalContainer.class})
public abstract class CorpseContainerMixin {

    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        if (playerInventory != null) {
            this.cachedPlayer = playerInventory.player;
        }
    }

    @Inject(method = "transferItems", at = @At("HEAD"), remap = false)
    private void transferItemsToCurios(CallbackInfo ci) {
        Object container = this;
        if (cachedPlayer == null || !cachedPlayer.isAlive()) {
            return;
        }

        if(!((CorpseContainerBase) container).isEditable()) {
            return;
        }

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(cachedPlayer).resolve();
        if (curiosOpt.isEmpty()) {
            return;
        }

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        List<ItemStack> curioItems = new ArrayList<>();
        List<Consumer<ItemStack>> clearActions = new ArrayList<>();

        if (container instanceof CorpseInventoryContainer) {
            CorpseInventoryContainer corpseContainer = (CorpseInventoryContainer) container;

            for (int i = 0; i < corpseContainer.slots.size(); i++) {
                if (i >= corpseContainer.getInventorySize()) break;

                Slot slot = corpseContainer.getSlot(i);
                if (slot == null) continue;

                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && corpse_Curios_Compat$shouldTransferCurio(stack)) {
                    curioItems.add(stack.copy());
                    final int slotIndex = i;
                    clearActions.add(s -> corpseContainer.getSlot(slotIndex).set(ItemStack.EMPTY));
                }
            }

            for (int i = 0; i < corpseContainer.getCorpse().getDeath().getAdditionalItems().size(); i++) {
                ItemStack stack = corpseContainer.getCorpse().getDeath().getAdditionalItems().get(i);
                if (!stack.isEmpty() && corpse_Curios_Compat$shouldTransferCurio(stack)) {
                    curioItems.add(stack.copy());
                    final int index = i;
                    clearActions.add(s -> corpseContainer.getCorpse().getDeath().getAdditionalItems().set(index, ItemStack.EMPTY));
                }
            }
        } else if (container instanceof CorpseAdditionalContainer) {
            CorpseAdditionalContainer additionalContainer = (CorpseAdditionalContainer) container;

            for (int i = 0; i < additionalContainer.getInventorySize(); i++) {
                Slot slot = additionalContainer.getSlot(i);
                if (slot == null) continue;

                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && corpse_Curios_Compat$shouldTransferCurio(stack)) {
                    curioItems.add(stack.copy());
                    final int slotIndex = i;
                    clearActions.add(s -> additionalContainer.getSlot(slotIndex).set(ItemStack.EMPTY));
                }
            }
        }

        // Separate items into priority (slot-adding) and regular items
        List<ItemStack> priorityItems = new ArrayList<>();
        List<Consumer<ItemStack>> priorityClearActions = new ArrayList<>();
        List<ItemStack> regularItems = new ArrayList<>();
        List<Consumer<ItemStack>> regularClearActions = new ArrayList<>();
        
        for (int i = 0; i < curioItems.size(); i++) {
            ItemStack stack = curioItems.get(i);
            CuriosSlotDataComponent.CurioSlotData slotData = CuriosSlotDataComponent.getCurioSlotData(stack);
            
            if (slotData != null && CuriosSlotDetector.doesItemAddSlots(stack, cachedPlayer, slotData.slotType())) {
                priorityItems.add(stack);
                priorityClearActions.add(clearActions.get(i));
            } else {
                regularItems.add(stack);
                regularClearActions.add(clearActions.get(i));
            }
        }

        // Process priority items immediately
        for (int i = 0; i < priorityItems.size(); i++) {
            ItemStack stack = priorityItems.get(i);
            if (corpse_Curios_Compat$tryTransferPreviouslyEquippedCurio(stack, curios)) {
                priorityClearActions.get(i).accept(stack);
            }
        }

        if (!regularItems.isEmpty() && !priorityItems.isEmpty()) {
            // Remove items from corpse BEFORE transferItems runs
            for (int i = 0; i < regularItems.size(); i++) {
                ItemStack itemCopy = regularItems.get(i).copy();
                regularClearActions.get(i).accept(regularItems.get(i));
                regularItems.set(i, itemCopy);
            }

            DelayedCurioHandler.scheduleCurioRestoration(cachedPlayer, regularItems);
        } else {
            for (int i = 0; i < regularItems.size(); i++) {
                ItemStack stack = regularItems.get(i);
                if (corpse_Curios_Compat$tryTransferPreviouslyEquippedCurio(stack, curios)) {
                    regularClearActions.get(i).accept(stack);
                }
            }
        }
    }

    @Unique
    private boolean corpse_Curios_Compat$shouldTransferCurio(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) {
            return false;
        }
        
        CuriosSlotDataComponent.CurioSlotData slotData = CuriosSlotDataComponent.getCurioSlotData(stack);
        if (slotData == null || !slotData.wasEquipped()) {
            return false;
        }
        
        if (Config.isItemBlacklisted(stack.getItem())) {
            return false;
        }

        return Config.shouldTransferCursedItems() || !Config.isItemCursed(stack);
    }

    @Unique
    private boolean corpse_Curios_Compat$tryTransferPreviouslyEquippedCurio(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;

        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) {
            return false;
        }

        CuriosSlotDataComponent.CurioSlotData slotData = CuriosSlotDataComponent.getCurioSlotData(stack);
        if (slotData == null || !slotData.wasEquipped()) {
            return false;
        }

        if (Config.isItemBlacklisted(stack.getItem())) {
            return false;
        }
        
        if (!Config.shouldTransferCursedItems() && Config.isItemCursed(stack)) {
            return false;
        }

        String slotType = slotData.slotType();
        int slotIndex = slotData.slotIndex();

        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0) {
            try {
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

                    CuriosSlotDataComponent.CurioSlotData existingSlotData = CuriosSlotDataComponent.getCurioSlotData(existingStack);
                    if (existingSlotData != null) {
                        if (!slotType.equals(existingSlotData.slotType()) ||
                                slotIndex != existingSlotData.slotIndex()) {

                            targetStacks.setStackInSlot(slotIndex, ItemStack.EMPTY);

                            ItemStack cleanStack = stack.copy();
                            CuriosSlotDataComponent.removeCurioSlotData(cleanStack);
                            targetStacks.setStackInSlot(slotIndex, cleanStack);

                            corpse_Curios_Compat$tryFindAlternativeSlot(existingStack, curios);
                            return true;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                return corpse_Curios_Compat$tryFindAlternativeSlot(stack, curios);
            }
        }

        return corpse_Curios_Compat$tryFindAlternativeSlot(stack, curios);
    }

    @Unique
    private boolean corpse_Curios_Compat$tryFindAlternativeSlot(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        CuriosSlotDataComponent.CurioSlotData slotData = CuriosSlotDataComponent.getCurioSlotData(stack);
        if (slotData == null || !slotData.wasEquipped()) {
            return false;
        }

        if (!Config.shouldTransferCursedItems() && Config.isItemCursed(stack)) {
            return false;
        }

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
                            CuriosSlotDataComponent.removeCurioSlotData(cleanStack);
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