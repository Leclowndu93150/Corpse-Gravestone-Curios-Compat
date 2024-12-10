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

import java.util.Map;
import java.util.Optional;

@Mixin(CorpseAdditionalContainer.class)
public abstract class CorpseAdditionalContainerMixin {

    private Player cachedPlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history, CallbackInfo ci) {
        this.cachedPlayer = playerInventory.player;
    }

    @Inject(method = "transferItems", at = @At("HEAD"), remap = false)
    private void transferItemsToCurios(CallbackInfo ci) {
        CorpseAdditionalContainer container = (CorpseAdditionalContainer) (Object) this;

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(this.cachedPlayer).resolve();

        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosOpt.get();

            for (int i = 0; i < container.getItems().size(); i++) {
                ItemStack stack = container.getSlot(i).getItem();

                if (!stack.isEmpty() && !CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) {
                    boolean itemTransferred = false;

                    for (Map.Entry<String, ICurioStacksHandler> entry : curiosHandler.getCurios().entrySet()) {
                        ICurioStacksHandler handler = entry.getValue();
                        String slotType = entry.getKey();

                        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                            for (int slot = 0; slot < handler.getSlots(); slot++) {
                                ItemStack currentSlotItem = handler.getStacks().getStackInSlot(slot);

                                if (currentSlotItem.isEmpty()) {
                                    handler.getStacks().setStackInSlot(slot, stack.copy());
                                    container.setItem(i, 1, ItemStack.EMPTY);
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
}