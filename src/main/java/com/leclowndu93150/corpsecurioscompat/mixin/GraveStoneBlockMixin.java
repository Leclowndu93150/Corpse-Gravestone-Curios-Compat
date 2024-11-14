package com.leclowndu93150.corpsecurioscompat.mixin;

import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

@Mixin(GraveStoneBlock.class)
public class GraveStoneBlockMixin {

    @Inject(
            method = "fillPlayerInventory",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void handleCuriosItems(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        NonNullList<ItemStack> additionalItems = cir.getReturnValue();
        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();

        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosOpt.get();

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (!stack.isEmpty()) {
                    boolean itemTransferred = false;

                    for (Map.Entry<String, ICurioStacksHandler> entry : curiosHandler.getCurios().entrySet()) {
                        ICurioStacksHandler handler = entry.getValue();
                        String slotType = entry.getKey();

                        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                            for (int slot = 0; slot < handler.getSlots(); slot++) {
                                ItemStack currentSlotItem = handler.getStacks().getStackInSlot(slot);

                                if (currentSlotItem.isEmpty()) {
                                    handler.getStacks().setStackInSlot(slot, stack.copy());
                                    player.getInventory().setItem(i, ItemStack.EMPTY);
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
        cir.setReturnValue(additionalItems);
    }
}
