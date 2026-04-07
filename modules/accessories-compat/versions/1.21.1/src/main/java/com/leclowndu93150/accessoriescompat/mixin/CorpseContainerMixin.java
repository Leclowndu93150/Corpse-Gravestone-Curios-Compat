package com.leclowndu93150.accessoriescompat.mixin;

import com.leclowndu93150.accessoriescompat.Config;
import com.leclowndu93150.accessoriescompat.data.AccessorySlotDataComponent;
import com.leclowndu93150.accessoriescompat.util.DelayedAccessoryHandler;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
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
    private void transferAccessories(CallbackInfo ci) {
        Object container = this;
        if (cachedPlayer == null || !cachedPlayer.isAlive()) return;
        if (!((CorpseContainerBase) container).isEditable()) return;

        AccessoriesCapability cap = AccessoriesCapability.get(cachedPlayer);
        if (cap == null) return;

        List<ItemStack> accessoryItems = new ArrayList<>();
        List<Consumer<ItemStack>> clearActions = new ArrayList<>();

        if (container instanceof CorpseInventoryContainer corpseContainer) {
            for (int i = 0; i < corpseContainer.slots.size(); i++) {
                if (i >= corpseContainer.getInventorySize()) break;
                Slot slot = corpseContainer.getSlot(i);
                if (slot == null) continue;
                ItemStack stack = slot.getItem();
                if (accessoriescompat$shouldTransfer(stack)) {
                    accessoryItems.add(stack.copy());
                    final int idx = i;
                    clearActions.add(s -> corpseContainer.getSlot(idx).set(ItemStack.EMPTY));
                }
            }

            for (int i = 0; i < corpseContainer.getCorpse().getDeath().getAdditionalItems().size(); i++) {
                ItemStack stack = corpseContainer.getCorpse().getDeath().getAdditionalItems().get(i);
                if (accessoriescompat$shouldTransfer(stack)) {
                    accessoryItems.add(stack.copy());
                    final int idx = i;
                    clearActions.add(s -> corpseContainer.getCorpse().getDeath().getAdditionalItems().set(idx, ItemStack.EMPTY));
                }
            }
        } else if (container instanceof CorpseAdditionalContainer additionalContainer) {
            for (int i = 0; i < additionalContainer.getInventorySize(); i++) {
                Slot slot = additionalContainer.getSlot(i);
                if (slot == null) continue;
                ItemStack stack = slot.getItem();
                if (accessoriescompat$shouldTransfer(stack)) {
                    accessoryItems.add(stack.copy());
                    final int idx = i;
                    clearActions.add(s -> additionalContainer.getSlot(idx).set(ItemStack.EMPTY));
                }
            }
        }

        for (int i = 0; i < accessoryItems.size(); i++) {
            ItemStack stack = accessoryItems.get(i);
            if (accessoriescompat$tryRestore(stack, cap)) {
                clearActions.get(i).accept(stack);
            }
        }
    }

    @Unique
    private boolean accessoriescompat$shouldTransfer(ItemStack stack) {
        if (stack.isEmpty()) return false;
        AccessorySlotDataComponent.AccessorySlotData data = AccessorySlotDataComponent.getSlotData(stack);
        if (data == null || !data.wasEquipped()) return false;
        if (Config.isItemBlacklisted(stack.getItem())) return false;
        return Config.shouldTransferCursedItems() || !Config.isItemCursed(stack);
    }

    @Unique
    private boolean accessoriescompat$tryRestore(ItemStack stack, AccessoriesCapability cap) {
        AccessorySlotDataComponent.AccessorySlotData data = AccessorySlotDataComponent.getSlotData(stack);
        if (data == null) return false;

        AccessoriesContainer container = cap.getContainers().get(data.slotName());
        if (container != null && data.slotIndex() >= 0 && data.slotIndex() < container.getSize()) {
            ItemStack existing = container.getAccessories().getItem(data.slotIndex());
            ItemStack clean = stack.copy();
            AccessorySlotDataComponent.removeSlotData(clean);

            if (existing.isEmpty()) {
                container.getAccessories().setItem(data.slotIndex(), clean);
                return true;
            } else {
                if (!cachedPlayer.getInventory().add(clean)) {
                    cachedPlayer.drop(clean, false);
                }
                return true;
            }
        }

        ItemStack clean = stack.copy();
        AccessorySlotDataComponent.removeSlotData(clean);
        if (!cachedPlayer.getInventory().add(clean)) {
            cachedPlayer.drop(clean, false);
        }
        return true;
    }
}
