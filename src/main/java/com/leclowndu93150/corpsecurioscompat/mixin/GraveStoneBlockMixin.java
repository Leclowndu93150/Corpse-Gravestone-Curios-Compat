package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.corpsecurioscompat.Config;
import com.leclowndu93150.corpsecurioscompat.CuriosSlotDataComponent;
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
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player);

        if (curiosHandler.isPresent()) {
            handleCuriosTransfer(curiosHandler.get(), death, unaddedItems);
        }

        handleNormalInventoryTransfer(player, death, unaddedItems);

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
            if (!stack.isEmpty() && tryTransferPreviouslyEquippedCurio(stack, curiosHandler.getCurios())) {
                inventory.set(i, ItemStack.EMPTY);
            }
        }
    }

    @Unique
    private boolean tryTransferPreviouslyEquippedCurio(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        if (stack.isEmpty()) return false;

        if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) {
            return false;
        }

        CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
        if (slotData == null || !slotData.wasEquipped()) {
            return false;
        }

        if (Config.isItemBlacklisted(stack.getItem())) {
            return false;
        }

        String slotType = slotData.slotType();
        int slotIndex = slotData.slotIndex();

        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0 && slotIndex < handler.getSlots()) {
            try {
                ItemStack existingStack = handler.getStacks().getStackInSlot(slotIndex);

                if (existingStack.isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                    handler.getStacks().setStackInSlot(slotIndex, cleanStack);
                    return true;
                }

                CuriosSlotDataComponent.CurioSlotData existingSlotData =
                        existingStack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                if (existingSlotData != null &&
                        (!slotType.equals(existingSlotData.slotType()) || slotIndex != existingSlotData.slotIndex())) {

                    handler.getStacks().setStackInSlot(slotIndex, ItemStack.EMPTY);

                    ItemStack cleanStack = stack.copy();
                    cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                    handler.getStacks().setStackInSlot(slotIndex, cleanStack);

                    tryFindAlternativeSlot(existingStack, curios);
                    return true;
                }
            } catch (IndexOutOfBoundsException e) {
                return tryFindAlternativeSlot(stack, curios);
            }
        }

        return tryFindAlternativeSlot(stack, curios);
    }

    @Unique
    private boolean tryFindAlternativeSlot(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
        if (slotData == null || !slotData.wasEquipped()) {
            return false;
        }

        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
            if (!CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(entry.getKey())) {
                continue;
            }

            ICurioStacksHandler handler = entry.getValue();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                try {
                    if (handler.getStacks().getStackInSlot(slot).isEmpty()) {
                        ItemStack cleanStack = stack.copy();
                        cleanStack.remove(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                        handler.getStacks().setStackInSlot(slot, cleanStack);
                        return true;
                    }
                } catch (IndexOutOfBoundsException ignored) {

                }
            }
        }

        return false;
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
        for (int i = 0; i < source.size() && i < destination.size(); i++) {
            ItemStack stack = source.get(i);
            if (!stack.isEmpty()) {
                CuriosSlotDataComponent.CurioSlotData slotData = stack.get(CuriosSlotDataComponent.CURIO_SLOT_DATA.get());
                if (slotData != null && slotData.wasEquipped()) {
                    continue;
                }

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