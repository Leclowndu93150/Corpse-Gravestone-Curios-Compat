package com.leclowndu93150.cosmeticcorpsecompat.mixin;

import com.leclowndu93150.cosmeticcorpsecompat.CosmeticArmorDeathEventHandler;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = {CorpseInventoryContainer.class, CorpseAdditionalContainer.class})
public abstract class CosmeticArmorCorpseMixin {
    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        if (playerInventory != null) {
            this.cachedPlayer = playerInventory.player;
        }
    }

    @Inject(method = "transferItems", at = @At("HEAD"), cancellable = true, remap = false)
    private void transferItemsToCosmeticArmor(CallbackInfo ci) {
        Object container = this;
        if (!cachedPlayer.isAlive()) return;

        if (!((CorpseContainerBase) container).isEditable()) {
            return;
        }

        UUID playerUUID = cachedPlayer.getUUID();
        CAStacksBase cosArmorInventory = CosArmorAPI.getCAStacks(playerUUID);

        boolean isCorpseInv = container instanceof CorpseInventoryContainer;
        boolean isAdditional = container instanceof CorpseAdditionalContainer;

        if (isCorpseInv) {
            CorpseInventoryContainer corpseContainer = (CorpseInventoryContainer) container;

            for (int i = 0; i < corpseContainer.slots.size(); i++) {
                if (i >= corpseContainer.getInventorySize()) break;

                Slot slot = corpseContainer.getSlot(i);
                ItemStack stack = slot.getItem();

                if (isCosmeticArmorItem(stack)) {
                    int cosmeticSlot = getCosmeticSlotIndex(stack);
                    boolean isSkinArmor = isSkinArmorItem(stack);

                    stack.remove(CosmeticArmorDeathEventHandler.COSMETIC_ARMOR_COMPONENT.get());
                    stack.remove(CosmeticArmorDeathEventHandler.SLOT_INDEX_COMPONENT.get());
                    stack.remove(CosmeticArmorDeathEventHandler.SKIN_ARMOR_COMPONENT.get());

                    cosArmorInventory.setStackInSlot(cosmeticSlot, stack);
                    cosArmorInventory.setSkinArmor(cosmeticSlot, isSkinArmor);
                    slot.set(ItemStack.EMPTY);
                }
            }

            for (int i = 0; i < corpseContainer.getCorpse().getDeath().getAdditionalItems().size(); i++) {
                ItemStack stack = corpseContainer.getCorpse().getDeath().getAdditionalItems().get(i);

                if (isCosmeticArmorItem(stack)) {
                    int cosmeticSlot = getCosmeticSlotIndex(stack);
                    boolean isSkinArmor = isSkinArmorItem(stack);

                    stack.remove(CosmeticArmorDeathEventHandler.COSMETIC_ARMOR_COMPONENT.get());
                    stack.remove(CosmeticArmorDeathEventHandler.SLOT_INDEX_COMPONENT.get());
                    stack.remove(CosmeticArmorDeathEventHandler.SKIN_ARMOR_COMPONENT.get());

                    cosArmorInventory.setStackInSlot(cosmeticSlot, stack);
                    cosArmorInventory.setSkinArmor(cosmeticSlot, isSkinArmor);
                    corpseContainer.getCorpse().getDeath().getAdditionalItems().set(i, ItemStack.EMPTY);
                }
            }
        } else if (isAdditional) {
            CorpseAdditionalContainer additionalContainer = (CorpseAdditionalContainer) container;

            for (int i = 0; i < additionalContainer.getInventorySize(); i++) {
                Slot slot = additionalContainer.getSlot(i);
                if (slot == null) continue;

                ItemStack stack = slot.getItem();

                if (isCosmeticArmorItem(stack)) {
                    int cosmeticSlot = getCosmeticSlotIndex(stack);
                    boolean isSkinArmor = isSkinArmorItem(stack);

                    stack.remove(CosmeticArmorDeathEventHandler.COSMETIC_ARMOR_COMPONENT.get());
                    stack.remove(CosmeticArmorDeathEventHandler.SLOT_INDEX_COMPONENT.get());
                    stack.remove(CosmeticArmorDeathEventHandler.SKIN_ARMOR_COMPONENT.get());

                    cosArmorInventory.setStackInSlot(cosmeticSlot, stack);
                    cosArmorInventory.setSkinArmor(cosmeticSlot, isSkinArmor);
                    slot.set(ItemStack.EMPTY);
                }
            }
        }
    }

    @Unique
    private boolean isCosmeticArmorItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Boolean isCosmeticArmor = stack.get(CosmeticArmorDeathEventHandler.COSMETIC_ARMOR_COMPONENT.get());
        return isCosmeticArmor != null && isCosmeticArmor;
    }

    @Unique
    private int getCosmeticSlotIndex(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Integer slotIndex = stack.get(CosmeticArmorDeathEventHandler.SLOT_INDEX_COMPONENT.get());
        return slotIndex != null ? slotIndex : 0;
    }

    @Unique
    private boolean isSkinArmorItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Boolean isSkinArmor = stack.get(CosmeticArmorDeathEventHandler.SKIN_ARMOR_COMPONENT.get());
        return isSkinArmor != null && isSkinArmor;
    }
}