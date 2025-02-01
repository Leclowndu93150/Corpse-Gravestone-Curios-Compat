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
public abstract class GraveStoneBlockMixin {

    @Inject(
            method = "fillPlayerInventory",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void handleItemTransfer(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        NonNullList<ItemStack> additionalItems = NonNullList.create();

        // Handle Curios
        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();
        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosOpt.get();

            // Process main inventory items
            for (int i = 0; i < death.getMainInventory().size(); i++) {
                ItemStack stack = death.getMainInventory().get(i);
                if (!stack.isEmpty() && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
                    handleCurioItem(stack, curiosHandler, additionalItems, death.getMainInventory(), i);
                }
            }

            // Process armor inventory items
            for (int i = 0; i < death.getArmorInventory().size(); i++) {
                ItemStack stack = death.getArmorInventory().get(i);
                if (!stack.isEmpty() && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
                    handleCurioItem(stack, curiosHandler, additionalItems, death.getArmorInventory(), i);
                }
            }

            // Process offhand inventory items
            for (int i = 0; i < death.getOffHandInventory().size(); i++) {
                ItemStack stack = death.getOffHandInventory().get(i);
                if (!stack.isEmpty() && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
                    handleCurioItem(stack, curiosHandler, additionalItems, death.getOffHandInventory(), i);
                }
            }

            // Process additional items
            for (int i = 0; i < death.getAdditionalItems().size(); i++) {
                ItemStack stack = death.getAdditionalItems().get(i);
                if (!stack.isEmpty() && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
                    handleCurioItem(stack, curiosHandler, additionalItems, death.getAdditionalItems(), i);
                }
            }
        }

        // Process non-curio items
        GraveStoneBlock block = (GraveStoneBlock) (Object) this;
        block.fillInventory(additionalItems, death.getMainInventory(), player.getInventory().items);
        block.fillInventory(additionalItems, death.getArmorInventory(), player.getInventory().armor);
        block.fillInventory(additionalItems, death.getOffHandInventory(), player.getInventory().offhand);
        additionalItems.addAll(death.getAdditionalItems());

        // Clear additional items
        death.getAdditionalItems().clear();

        // Try to add items to player's inventory
        NonNullList<ItemStack> restItems = NonNullList.create();
        for (ItemStack stack : additionalItems) {
            if (!player.getInventory().add(stack)) {
                restItems.add(stack);
            }
        }

        cir.setReturnValue(restItems);
    }

    private void handleCurioItem(ItemStack stack, ICuriosItemHandler curiosHandler, NonNullList<ItemStack> additionalItems, NonNullList<ItemStack> sourceInventory, int sourceIndex) {
        boolean transferred = false;

        for (Map.Entry<String, ICurioStacksHandler> entry : curiosHandler.getCurios().entrySet()) {
            String slotType = entry.getKey();
            ICurioStacksHandler handler = entry.getValue();

            if (handler != null && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack currentSlot = handler.getStacks().getStackInSlot(slot);
                    if (currentSlot.isEmpty()) {
                        handler.getStacks().setStackInSlot(slot, stack.copy());
                        sourceInventory.set(sourceIndex, ItemStack.EMPTY);
                        transferred = true;
                        break;
                    } else {
                        additionalItems.add(currentSlot.copy());
                        handler.getStacks().setStackInSlot(slot, stack.copy());
                        sourceInventory.set(sourceIndex, ItemStack.EMPTY);
                        transferred = true;
                        break;
                    }
                }
            }
            if (transferred) break;
        }

        if (!transferred) {
            additionalItems.add(stack.copy());
            sourceInventory.set(sourceIndex, ItemStack.EMPTY);
        }
    }
}