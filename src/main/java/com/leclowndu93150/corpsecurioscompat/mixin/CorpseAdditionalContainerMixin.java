package com.leclowndu93150.corpsecurioscompat.mixin;

import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import java.util.Map;
import java.util.Optional;

@Mixin(de.maxhenkel.corpse.gui.CorpseAdditionalContainer.class)
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
        de.maxhenkel.corpse.gui.CorpseAdditionalContainer container = (de.maxhenkel.corpse.gui.CorpseAdditionalContainer) (Object) this;

        if (!container.isEditable() || this.cachedPlayer == null) {
            return;
        }

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(this.cachedPlayer).resolve();
        if (!curiosOpt.isPresent()) {
            return;
        }

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        NonNullList<ItemStack> nonCurioItems = NonNullList.create();

        for (int i = 0; i < container.getItems().size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
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
                    if (!transferred) {
                        nonCurioItems.add(stack.copy());
                        container.getSlot(i).set(ItemStack.EMPTY);
                    }
                } else {
                    nonCurioItems.add(stack.copy());
                    container.getSlot(i).set(ItemStack.EMPTY);
                }
            }
        }

        PlayerMainInvWrapper playerWrapper = new PlayerMainInvWrapper(this.cachedPlayer.getInventory());
        for (ItemStack stack : nonCurioItems) {
            for (int j = 0; j < playerWrapper.getSlots(); j++) {
                stack = playerWrapper.insertItem(j, stack, false);
                if (stack.isEmpty()) {
                    break;
                }
            }

            if (!stack.isEmpty()) {
                for (int i = 0; i < container.getItems().size(); i++) {
                    if (container.getSlot(i).getItem().isEmpty()) {
                        container.getSlot(i).set(stack);
                        break;
                    }
                }
            }
        }

        ci.cancel();
    }
}