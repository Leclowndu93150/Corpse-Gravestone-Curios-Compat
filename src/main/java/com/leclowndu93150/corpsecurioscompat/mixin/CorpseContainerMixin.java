package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.corpsecurioscompat.Config;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import net.minecraft.nbt.CompoundTag;
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

    private static final String CURIO_SLOT_TAG = "CorpseCuriosSlot";
    private static final String CURIO_SLOT_TYPE = "SlotType";
    private static final String CURIO_SLOT_INDEX = "SlotIndex";
    private static final String WAS_EQUIPPED = "WasEquipped";

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
        if (!cachedPlayer.isAlive()) {
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

        if (container instanceof CorpseInventoryContainer) {
            CorpseInventoryContainer corpseContainer = (CorpseInventoryContainer) container;

            for (int i = 0; i < corpseContainer.slots.size(); i++) {
                if (i >= corpseContainer.getInventorySize()) break;

                Slot slot = corpseContainer.getSlot(i);
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

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CURIO_SLOT_TAG)) {
            return false;
        }

        CompoundTag slotData = tag.getCompound(CURIO_SLOT_TAG);
        boolean wasEquipped = slotData.getBoolean(WAS_EQUIPPED);

        if (!wasEquipped) {
            return false;
        }

        if (Config.isItemBlacklisted(stack.getItem())) {
            return false;
        }

        String slotType = slotData.getString(CURIO_SLOT_TYPE);
        int slotIndex = slotData.getInt(CURIO_SLOT_INDEX);

        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0 && slotIndex < handler.getSlots()) {
            ItemStack existingStack = handler.getStacks().getStackInSlot(slotIndex);

            if (existingStack.isEmpty()) {
                ItemStack cleanStack = stack.copy();
                cleanSlotData(cleanStack);
                handler.getStacks().setStackInSlot(slotIndex, cleanStack);
                return true;
            }

            CompoundTag existingTag = existingStack.getTag();
            if (existingTag != null && existingTag.contains(CURIO_SLOT_TAG)) {
                CompoundTag existingSlotData = existingTag.getCompound(CURIO_SLOT_TAG);
                if (!slotType.equals(existingSlotData.getString(CURIO_SLOT_TYPE)) ||
                        slotIndex != existingSlotData.getInt(CURIO_SLOT_INDEX)) {

                    handler.getStacks().setStackInSlot(slotIndex, ItemStack.EMPTY);

                    ItemStack cleanStack = stack.copy();
                    cleanSlotData(cleanStack);
                    handler.getStacks().setStackInSlot(slotIndex, cleanStack);


                    tryFindAlternativeSlot(existingStack, curios);
                    return true;
                }
            }
        }

        return tryFindAlternativeSlot(stack, curios);
    }

    private boolean tryFindAlternativeSlot(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CURIO_SLOT_TAG)) {
            return false;
        }

        CompoundTag slotData = tag.getCompound(CURIO_SLOT_TAG);
        if (!slotData.getBoolean(WAS_EQUIPPED)) {
            return false;
        }

        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            if (!CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(entry.getKey())) {
                continue;
            }

            ICurioStacksHandler handler = entry.getValue();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (handler.getStacks().getStackInSlot(slot).isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    cleanSlotData(cleanStack);
                    handler.getStacks().setStackInSlot(slot, cleanStack);
                    return true;
                }
            }
        }

        return false;
    }

    private void cleanSlotData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(CURIO_SLOT_TAG)) {
            tag.remove(CURIO_SLOT_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}