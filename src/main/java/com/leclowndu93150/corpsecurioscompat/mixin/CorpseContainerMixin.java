package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.corpsecurioscompat.Config;
import com.leclowndu93150.corpsecurioscompat.CuriosSlotDataComponent;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

@Mixin(value = {CorpseInventoryContainer.class, CorpseAdditionalContainer.class})
public abstract class CorpseContainerMixin {
    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        if (playerInventory != null) {
            this.cachedPlayer = playerInventory.player;
        }
    }

    @Inject(method = "transferItems", at = @At("HEAD"), cancellable = true, remap = false)
    private void transferItemsToCurios(CallbackInfo ci) {
        Object container = this;
        if (cachedPlayer == null || !cachedPlayer.isAlive()) {
            return;
        }

        if(!((CorpseContainerBase) container).isEditable()) {
            return;
        }

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosInventory(cachedPlayer);
        if (curiosOpt.isEmpty()) {
            return;
        }

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        if (container instanceof CorpseInventoryContainer) {
            CorpseInventoryContainer corpseContainer = (CorpseInventoryContainer) container;

            for (int i = 0; i < corpseContainer.slots.size(); i++) {
                if (i >= corpseContainer.getInventorySize()) break;

                Slot slot = corpseContainer.getSlot(i);
                if (slot == null) continue;

                ItemStack stack = slot.getItem();

                if (!stack.isEmpty() && tryTransferPreviouslyEquippedCurio(stack, curios)) {
                    slot.set(ItemStack.EMPTY);
                }
            }

            for (int i = 0; i < corpseContainer.getCorpse().getDeath().getAdditionalItems().size(); i++) {
                ItemStack stack = corpseContainer.getCorpse().getDeath().getAdditionalItems().get(i);
                if (!stack.isEmpty() && tryTransferPreviouslyEquippedCurio(stack, curios)) {
                    corpseContainer.getCorpse().getDeath().getAdditionalItems().set(i, ItemStack.EMPTY);
                }
            }
        } else if (container instanceof CorpseAdditionalContainer) {
            CorpseAdditionalContainer additionalContainer = (CorpseAdditionalContainer) container;

            for (int i = 0; i < additionalContainer.getInventorySize(); i++) {
                Slot slot = additionalContainer.getSlot(i);
                if (slot == null) continue;

                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && tryTransferPreviouslyEquippedCurio(stack, curios)) {
                    slot.set(ItemStack.EMPTY);
                }
            }
        }
    }

    private boolean tryTransferPreviouslyEquippedCurio(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;

        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) {
            return false;
        }

        CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
        if (slotData == null || !slotData.wasEquipped()) {
            return false;
        }

        if (Config.isItemBlacklisted(stack.getItem())) {
            return false;
        }

        String slotType = slotData.slotType();
        int slotIndex = slotData.slotIndex();

        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0) {
            try {
                // Determine which stacks handler to use based on whether it's cosmetic
                var targetStacks = slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
                
                if (slotIndex < targetStacks.getSlots()) {
                    ItemStack existingStack = targetStacks.getStackInSlot(slotIndex);

                    if (existingStack.isEmpty()) {
                        ItemStack cleanStack = stack.copy();
                        cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                        targetStacks.setStackInSlot(slotIndex, cleanStack);
                        return true;
                    }

                    CuriosSlotDataComponent.CurioSlotData existingSlotData =
                            existingStack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                    if (existingSlotData != null &&
                            (!slotType.equals(existingSlotData.slotType()) || slotIndex != existingSlotData.slotIndex() || 
                             slotData.isCosmetic() != existingSlotData.isCosmetic())) {

                        targetStacks.setStackInSlot(slotIndex, ItemStack.EMPTY);

                        ItemStack cleanStack = stack.copy();
                        cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                        targetStacks.setStackInSlot(slotIndex, cleanStack);

                        tryFindAlternativeSlot(existingStack, curios);
                        return true;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                return tryFindAlternativeSlot(stack, curios);
            }
        }

        return tryFindAlternativeSlot(stack, curios);
    }

    private boolean tryFindAlternativeSlot(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
        if (slotData == null || !slotData.wasEquipped()) {
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
                            cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                            stacks.setStackInSlot(slot, cleanStack);
                            return true;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println("CorpseCuriosCompat: Index out of bounds while trying to transfer curio stack: " + e.getMessage());
                    }
                }
            }
        }

        return false;
    }
}