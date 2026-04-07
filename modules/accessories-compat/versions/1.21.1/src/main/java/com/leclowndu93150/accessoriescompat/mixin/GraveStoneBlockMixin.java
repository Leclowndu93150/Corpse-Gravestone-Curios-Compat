package com.leclowndu93150.accessoriescompat.mixin;

import com.leclowndu93150.accessoriescompat.Config;
import com.leclowndu93150.accessoriescompat.data.AccessorySlotDataComponent;
import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(GraveStoneBlock.class)
public abstract class GraveStoneBlockMixin {

    @Unique
    private final ThreadLocal<List<ItemStack>> accessoriescompat$overflow = ThreadLocal.withInitial(ArrayList::new);

    @Inject(method = "fillPlayerInventory", at = @At("HEAD"), remap = false)
    private void accessoriescompat$extractAccessories(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        accessoriescompat$overflow.get().clear();

        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return;

        List<NonNullList<ItemStack>> inventories = List.of(
                death.getMainInventory(), death.getArmorInventory(),
                death.getOffHandInventory(), death.getAdditionalItems()
        );

        for (NonNullList<ItemStack> inventory : inventories) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.get(i);
                if (stack.isEmpty()) continue;

                AccessorySlotDataComponent.AccessorySlotData data = AccessorySlotDataComponent.getSlotData(stack);
                if (data == null || !data.wasEquipped()) continue;
                if (Config.isItemBlacklisted(stack.getItem())) continue;
                if (!Config.shouldTransferCursedItems() && Config.isItemCursed(stack)) continue;

                ItemStack copy = stack.copy();
                inventory.set(i, ItemStack.EMPTY);

                if (!accessoriescompat$tryRestore(copy, player, cap)) {
                    accessoriescompat$overflow.get().add(copy);
                }
            }
        }
    }

    @Inject(method = "fillPlayerInventory", at = @At("RETURN"), remap = false)
    private void accessoriescompat$addOverflow(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        NonNullList<ItemStack> result = cir.getReturnValue();
        List<ItemStack> overflow = accessoriescompat$overflow.get();
        for (ItemStack stack : overflow) {
            if (!player.getInventory().add(stack)) {
                result.add(stack);
            }
        }
        overflow.clear();
    }

    @Unique
    private boolean accessoriescompat$tryRestore(ItemStack stack, Player player, AccessoriesCapability cap) {
        AccessorySlotDataComponent.AccessorySlotData data = AccessorySlotDataComponent.getSlotData(stack);
        if (data == null) return false;

        AccessoriesContainer container = cap.getContainers().get(data.slotName());
        if (container != null && data.slotIndex() >= 0 && data.slotIndex() < container.getSize()) {
            ItemStack existing = container.getAccessories().getItem(data.slotIndex());
            ItemStack clean = stack.copy();
            AccessorySlotDataComponent.removeSlotData(clean);

            if (existing.isEmpty()) {
                container.getAccessories().setItem(data.slotIndex(), clean);
                return true;
            } else {
                if (!player.getInventory().add(clean)) {
                    player.drop(clean, false);
                }
                return true;
            }
        }

        ItemStack clean = stack.copy();
        AccessorySlotDataComponent.removeSlotData(clean);
        if (!player.getInventory().add(clean)) {
            player.drop(clean, false);
        }
        return true;
    }
}
