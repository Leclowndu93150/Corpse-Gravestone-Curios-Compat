package com.leclowndu93150.corpsecurioscompat.mixin;

import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(CorpseAdditionalContainer.class)
public abstract class CorpseAdditionalContainerMixin {

    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        if (playerInventory != null) {
            this.cachedPlayer = playerInventory.player;
        }
    }

    @Inject(method = "transferItems", at = @At("HEAD"), cancellable = true)
    private void transferItemsToCurios(CallbackInfo ci) {
        if (this.cachedPlayer == null) return;

        de.maxhenkel.corpse.gui.CorpseAdditionalContainer container =
                (de.maxhenkel.corpse.gui.CorpseAdditionalContainer) (Object) this;

        if (!container.isEditable()) {
            ci.cancel();
            return;
        }

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(this.cachedPlayer);
        if (!curiosOpt.isPresent()) return;

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        List<Integer> processedSlots = new ArrayList<>();
        for (int i = 0; i < container.getItems().size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (stack.isEmpty() || CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) {
                continue;
            }

            boolean itemTransferred = false;
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                if (entry == null || entry.getValue() == null) continue;

                ICurioStacksHandler handler = entry.getValue();
                String slotType = entry.getKey();

                if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        ItemStack currentSlot = handler.getStacks().getStackInSlot(slot);

                        if (currentSlot.isEmpty()) {
                            handler.getStacks().setStackInSlot(slot, stack.copy());
                            container.setItem(i, 1, ItemStack.EMPTY);
                            itemTransferred = true;
                            processedSlots.add(i);
                            break;
                        } else if (currentSlot.getItem() == stack.getItem()
                                && ItemStack.isSameItemSameComponents(currentSlot, stack)
                                && currentSlot.getCount() < currentSlot.getMaxStackSize()) {
                            int canAdd = currentSlot.getMaxStackSize() - currentSlot.getCount();
                            int toAdd = Math.min(canAdd, stack.getCount());

                            currentSlot.grow(toAdd);
                            stack.shrink(toAdd);

                            if (stack.isEmpty()) {
                                container.setItem(i, 1, ItemStack.EMPTY);
                                itemTransferred = true;
                                processedSlots.add(i);
                                break;
                            }
                        }
                    }
                }
                if (itemTransferred) break;
            }
        }
    }
}