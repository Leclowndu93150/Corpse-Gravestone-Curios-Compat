package com.leclowndu93150.corpsecurioscompat.mixin;

import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

@Mixin(GraveStoneBlock.class)
public abstract class GraveStoneBlockMixin {

    /**
     * @author Leclowndu93150
     * @reason Overwrite the fillPlayerInventory method to handle Curios items
     */
    @Overwrite(remap = false)
    public NonNullList<ItemStack> fillPlayerInventory(Player player, Death death) {
        NonNullList<ItemStack> unaddedItems = NonNullList.create();
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(player);

        // I had to add steps because i am so retarded and can't figure out how to do it correctly

        // Step 1: Handle Curios items
        if (curiosHandler.isPresent()) {
            handleCuriosTransfer(curiosHandler.get(), death, unaddedItems);
        }

        // Step 2: Handle regular inventory
        handleNormalInventoryTransfer(player, death, unaddedItems);

        // Step 3: Try to add remaining items to player inventory
        NonNullList<ItemStack> overflow = NonNullList.create();
        for (ItemStack stack : unaddedItems) {
            if (!player.getInventory().add(stack)) {
                overflow.add(stack);
            }
        }

        death.getAdditionalItems().clear();
        return overflow;
    }

    @Unique
    private void handleCuriosTransfer(ICuriosItemHandler curiosHandler, Death death, NonNullList<ItemStack> overflow) {
        transferCuriosFromInventory(death.getMainInventory(), curiosHandler, overflow);
        transferCuriosFromInventory(death.getArmorInventory(), curiosHandler, overflow);
        transferCuriosFromInventory(death.getOffHandInventory(), curiosHandler, overflow);
        transferCuriosFromInventory(death.getAdditionalItems(), curiosHandler, overflow);
    }

    @Unique
    private void transferCuriosFromInventory(NonNullList<ItemStack> inventory, ICuriosItemHandler curiosHandler, NonNullList<ItemStack> overflow) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).size() > 0) {
                boolean transferred = false;

                for (Map.Entry<String, ICurioStacksHandler> entry : curiosHandler.getCurios().entrySet()) {
                    String slotType = entry.getKey();
                    ICurioStacksHandler handler = entry.getValue();

                    if (handler != null && CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(slotType)) {
                        for (int slot = 0; slot < handler.getSlots(); slot++) {
                            ItemStack existingStack = handler.getStacks().getStackInSlot(slot);
                            if (existingStack.isEmpty()) {
                                handler.getStacks().setStackInSlot(slot, stack.copy());
                                inventory.set(i, ItemStack.EMPTY);
                                transferred = true;
                                break;
                            } else {
                                overflow.add(existingStack.copy());
                                handler.getStacks().setStackInSlot(slot, stack.copy());
                                inventory.set(i, ItemStack.EMPTY);
                                transferred = true;
                                break;
                            }
                        }
                    }
                    if (transferred) break;
                }

                if (!transferred) {
                    overflow.add(stack.copy());
                    inventory.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Unique
    private void handleNormalInventoryTransfer(Player player, Death death, NonNullList<ItemStack> overflow) {
        transferInventory(death.getMainInventory(), player.getInventory().items, overflow);
        transferInventory(death.getArmorInventory(), player.getInventory().armor, overflow);
        transferInventory(death.getOffHandInventory(), player.getInventory().offhand, overflow);
        death.getAdditionalItems().forEach(overflow::add);
    }

    @Unique
    private void transferInventory(NonNullList<ItemStack> source, NonNullList<ItemStack> destination, NonNullList<ItemStack> overflow) {
        for (int i = 0; i < source.size(); i++) {
            ItemStack stack = source.get(i);
            if (!stack.isEmpty()) {
                ItemStack currentStack = destination.get(i);
                if (!currentStack.isEmpty()) {
                    overflow.add(currentStack);
                }
                destination.set(i, stack);
                source.set(i, ItemStack.EMPTY);
            }
        }
    }
}