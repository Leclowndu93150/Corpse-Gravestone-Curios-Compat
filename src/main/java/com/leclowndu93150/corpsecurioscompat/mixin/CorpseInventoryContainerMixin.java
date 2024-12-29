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

@Mixin(CorpseInventoryContainer.class)
public abstract class CorpseInventoryContainerMixin {
    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        if (playerInventory != null) {
            this.cachedPlayer = playerInventory.player;
        }
    }

    @Inject(method = "transferItems", at = @At("HEAD"), cancellable = true)
    private void transferItemsToCurios(CallbackInfo ci) {
        CorpseInventoryContainer container = (CorpseInventoryContainer) (Object) this;

        if (!container.isEditable() || this.cachedPlayer == null) {
            return;
        }

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(this.cachedPlayer).resolve();
        if (!curiosOpt.isPresent()) {
            return;
        }

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        for (int i = 0; i < container.getItems().size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!stack.isEmpty() && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
                boolean transferred = false;
                for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                    if (entry.getValue() != null && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(entry.getKey())) {
                        ICurioStacksHandler handler = entry.getValue();
                        for (int slot = 0; slot < handler.getSlots(); slot++) {
                            ItemStack currentSlot = handler.getStacks().getStackInSlot(slot);
                            if (currentSlot.isEmpty()) {
                                handler.getStacks().setStackInSlot(slot, stack.copy());
                                container.getSlot(i).set(ItemStack.EMPTY);
                                transferred = true;
                                break;
                            }
                        }
                        if (transferred) break;
                    }
                }
            }
        }

    }
}