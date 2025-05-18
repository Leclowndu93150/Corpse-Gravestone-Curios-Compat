package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.corpsecurioscompat.Config;
import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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

    @Unique
    private static final String CURIO_SLOT_TAG = "CorpseCuriosSlot";

    @Unique
    private static final String CURIO_SLOT_TYPE = "SlotType";

    @Unique
    private static final String CURIO_SLOT_INDEX = "SlotIndex";

    @Unique
    private static final String WAS_EQUIPPED = "WasEquipped";

    /**
     * @author Leclowndu93150
     * @reason Overwrite the fillPlayerInventory method to handle Curios items
     */
    @Overwrite(remap = false)
    public NonNullList<ItemStack> fillPlayerInventory(Player player, Death death) {
        NonNullList<ItemStack> unaddedItems = NonNullList.create();

        Optional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();

        if (curiosOpt.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosOpt.get();
            Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

            transferCuriosItems(death.getMainInventory(), curios);
            transferCuriosItems(death.getArmorInventory(), curios);
            transferCuriosItems(death.getOffHandInventory(), curios);
            transferCuriosItems(death.getAdditionalItems(), curios);
        }

        transferInventory(death.getMainInventory(), player.getInventory().items, unaddedItems);
        transferInventory(death.getArmorInventory(), player.getInventory().armor, unaddedItems);
        transferInventory(death.getOffHandInventory(), player.getInventory().offhand, unaddedItems);

        death.getAdditionalItems().forEach(unaddedItems::add);

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
    private void transferCuriosItems(NonNullList<ItemStack> inventory, Map<String, ICurioStacksHandler> curios) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && tryTransferPreviouslyEquippedCurio(stack, curios)) {
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

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CURIO_SLOT_TAG)) {
            return false;
        }

        CompoundTag slotData = tag.getCompound(CURIO_SLOT_TAG);
        boolean wasEquipped = slotData.getBoolean(WAS_EQUIPPED);
        if (!wasEquipped) {
            return false;
        }

        if (Config.isItemBlacklisted(stack.getItem())) {
            return false;
        }

        String slotType = slotData.getString(CURIO_SLOT_TYPE);
        int slotIndex = slotData.getInt(CURIO_SLOT_INDEX);

        ICurioStacksHandler handler = curios.get(slotType);
        if (handler != null && slotIndex >= 0 && slotIndex < handler.getSlots()) {
            try {
                ItemStack existingStack = handler.getStacks().getStackInSlot(slotIndex);

                if (existingStack.isEmpty()) {
                    ItemStack cleanStack = stack.copy();
                    cleanNbtData(cleanStack);
                    handler.getStacks().setStackInSlot(slotIndex, cleanStack);
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return tryFindAnyCompatibleSlot(stack, curios);
    }

    @Unique
    private boolean tryFindAnyCompatibleSlot(ItemStack stack, Map<String, ICurioStacksHandler> curios) {
        for (String slotType : CuriosApi.getCuriosHelper().getCurioTags(stack.getItem())) {
            ICurioStacksHandler handler = curios.get(slotType);
            if (handler == null) continue;

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                try {
                    if (handler.getStacks().getStackInSlot(slot).isEmpty()) {
                        ItemStack cleanStack = stack.copy();
                        cleanNbtData(cleanStack);
                        handler.getStacks().setStackInSlot(slot, cleanStack);
                        return true;
                    }
                } catch (Exception ignored) {

                }
            }
        }

        return false;
    }

    @Unique
    private void cleanNbtData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(CURIO_SLOT_TAG)) {
            tag.remove(CURIO_SLOT_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    @Unique
    private void transferInventory(NonNullList<ItemStack> source, NonNullList<ItemStack> destination, NonNullList<ItemStack> unaddedItems) {
        for (int i = 0; i < source.size() && i < destination.size(); i++) {
            ItemStack stack = source.get(i);
            if (!stack.isEmpty()) {
                ItemStack currentStack = destination.get(i);
                if (!currentStack.isEmpty()) {
                    unaddedItems.add(currentStack);
                }
                destination.set(i, stack);
                source.set(i, ItemStack.EMPTY);
            }
        }
    }
}