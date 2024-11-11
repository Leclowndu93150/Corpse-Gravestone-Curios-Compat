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
        this.cachedPlayer = playerInventory.player;
    }

    @Inject(method = "transferItems", at = @At("TAIL"))
    private void transferItemsToCurios(CallbackInfo ci) {
        CorpseInventoryContainer container = (CorpseInventoryContainer) (Object) this;
        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(this.cachedPlayer);

        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosOpt.get();

            for (int i = 0; i < this.cachedPlayer.getInventory().getContainerSize(); i++) {
                ItemStack stack = this.cachedPlayer.getInventory().getItem(i);

                if (!stack.isEmpty()) {
                    boolean itemTransferred = false;

                    for (Map.Entry<String, ICurioStacksHandler> entry : curiosHandler.getCurios().entrySet()) {
                        ICurioStacksHandler handler = entry.getValue();
                        String slotType = entry.getKey();

                        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                            for (int slot = 0; slot < handler.getSlots(); slot++) {
                                ItemStack currentSlotItem = handler.getStacks().getStackInSlot(slot);

                                if (currentSlotItem.isEmpty()) {
                                    System.out.println("Transfering item to curio slot from player inventory");
                                    handler.getStacks().setStackInSlot(slot, stack.copy());
                                    this.cachedPlayer.getInventory().setItem(i, ItemStack.EMPTY); // Remove the item from the player's inventory
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
