package com.leclowndu93150.corpsecurioscompat.mixin;

import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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

@Mixin(value = CorpseInventoryContainer.class, remap = false)
public abstract class CorpseInventoryContainerMixin {

    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        if (playerInventory != null) {
            this.cachedPlayer = playerInventory.player;
        }
    }

    @Inject(method = "transferItems", at = @At("TAIL"))
    private void transferItemsToCurios(CallbackInfo ci) {
        if (this.cachedPlayer == null) {
            return;
        }

        CorpseInventoryContainer container = (CorpseInventoryContainer) (Object) this;
        if (container == null) {
            return;
        }

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(this.cachedPlayer).resolve();

        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosOpt.get();

            if (this.cachedPlayer.getInventory() == null) {
                return;
            }

            for (int i = 0; i < this.cachedPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = this.cachedPlayer.getInventory().getItem(i);

                if (stack == null || stack.isEmpty()) {
                    continue;
                }

                boolean itemTransferred = false;

                Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();
                if (curios == null) {
                    continue;
                }

                for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                    if (entry == null || entry.getValue() == null || entry.getKey() == null) {
                        continue;
                    }

                    ICurioStacksHandler handler = entry.getValue();
                    String slotType = entry.getKey();

                    if (stack.getItem() == null) {
                        continue;
                    }

                    if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                        for (int slot = 0; slot < handler.getSlots(); slot++) {
                            ItemStack currentSlotItem = handler.getStacks().getStackInSlot(slot);

                            if (currentSlotItem == null || currentSlotItem.isEmpty()) {
                                handler.getStacks().setStackInSlot(slot, stack.copy());
                                this.cachedPlayer.getInventory().setItem(i, ItemStack.EMPTY);
                                itemTransferred = true;
                                break;
                            }
                        }
                    }
                    if (itemTransferred) {
                        break;
                    }
                }
            }
        }
    }
}