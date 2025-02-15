package com.leclowndu93150.corpsecurioscompat.mixin;

import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
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
        if (!cachedPlayer.isAlive()) return;

        boolean isCorpseInv = container instanceof CorpseInventoryContainer;
        boolean isAdditional = container instanceof CorpseAdditionalContainer;

        java.util.List<ItemStack> items = isCorpseInv ?
                ((CorpseInventoryContainer)container).getItems() :
                ((CorpseAdditionalContainer)container).getItems();

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(cachedPlayer).resolve();
        if (curiosOpt.isEmpty()) {
            return;
        }

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty() || CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) continue;

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                if (!CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(entry.getKey())) continue;

                ICurioStacksHandler handler = entry.getValue();
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    if (!handler.getStacks().getStackInSlot(slot).isEmpty()) continue;

                    handler.getStacks().setStackInSlot(slot, stack.copy());
                    if (isCorpseInv) {
                        ((CorpseInventoryContainer)container).getSlot(i).set(ItemStack.EMPTY);
                    } else {
                        ((CorpseAdditionalContainer)container).setItem(i, 1, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }
    }
}
