package com.leclowndu93150.corpsecurioscompat.mixin;

import com.leclowndu93150.baguettelib.curios.BaguetteCuriosData;
import com.leclowndu93150.baguettelib.curios.CurioSlotData;
import com.leclowndu93150.corpsecurioscompat.Config;
import com.leclowndu93150.corpsecurioscompat.util.CuriosSlotDetector;
import com.leclowndu93150.corpsecurioscompat.util.DelayedCurioHandler;
import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(GraveStoneBlock.class)
public abstract class GraveStoneBlockMixin {

    @Unique
    private final ThreadLocal<List<ItemStack>> gravestonecurioscompat$overflow = ThreadLocal.withInitial(ArrayList::new);

    @Inject(method = "fillPlayerInventory", at = @At("HEAD"), remap = false)
    private void gravestonecurioscompat$extractCurios(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        gravestonecurioscompat$overflow.get().clear();

        var curiosOpt = CuriosApi.getCuriosInventory(player);
        if (curiosOpt.isEmpty()) return;

        ICuriosItemHandler curiosHandler = curiosOpt.get();
        Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();

        List<ItemStack> priorityItems = new ArrayList<>();
        List<ItemStack> regularItems = new ArrayList<>();

        List<NonNullList<ItemStack>> inventories = List.of(
                death.getMainInventory(), death.getArmorInventory(),
                death.getOffHandInventory(), death.getAdditionalItems()
        );

        for (NonNullList<ItemStack> inventory : inventories) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.get(i);
                if (stack.isEmpty()) continue;

                CurioSlotData slotData = BaguetteCuriosData.getSlotData(stack);
                if (slotData == null || !slotData.wasEquipped()) continue;
                if (CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).isEmpty()) continue;
                if (Config.isItemBlacklisted(stack.getItem())) continue;
                if (!Config.shouldTransferCursedItems() && Config.isItemCursed(stack)) continue;

                ItemStack copy = stack.copy();
                inventory.set(i, ItemStack.EMPTY);

                if (CuriosSlotDetector.doesItemAddSlots(copy, player, slotData.slotType())) {
                    priorityItems.add(copy);
                } else {
                    regularItems.add(copy);
                }
            }
        }

        for (ItemStack stack : priorityItems) {
            if (!gravestonecurioscompat$tryRestore(stack, player, curios)) {
                gravestonecurioscompat$overflow.get().add(stack);
            }
        }

        if (!regularItems.isEmpty() && !priorityItems.isEmpty()) {
            DelayedCurioHandler.scheduleCurioRestoration(player, regularItems);
        } else {
            for (ItemStack stack : regularItems) {
                if (!gravestonecurioscompat$tryRestore(stack, player, curios)) {
                    gravestonecurioscompat$overflow.get().add(stack);
                }
            }
        }
    }

    @Inject(method = "fillPlayerInventory", at = @At("RETURN"), remap = false)
    private void gravestonecurioscompat$addOverflow(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        NonNullList<ItemStack> result = cir.getReturnValue();
        List<ItemStack> overflow = gravestonecurioscompat$overflow.get();
        for (ItemStack stack : overflow) {
            if (!player.getInventory().add(stack)) {
                result.add(stack);
            }
        }
        overflow.clear();
    }

    @Unique
    private boolean gravestonecurioscompat$tryRestore(ItemStack stack, Player player, Map<String, ICurioStacksHandler> curios) {
        CurioSlotData slotData = BaguetteCuriosData.getSlotData(stack);
        if (slotData == null) return false;

        ICurioStacksHandler handler = curios.get(slotData.slotType());
        if (handler != null && slotData.slotIndex() >= 0) {
            var targetStacks = slotData.isCosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
            if (slotData.slotIndex() < targetStacks.getSlots()) {
                ItemStack existing = targetStacks.getStackInSlot(slotData.slotIndex());
                ItemStack clean = stack.copy();
                BaguetteCuriosData.removeSlotData(clean);

                if (existing.isEmpty()) {
                    targetStacks.setStackInSlot(slotData.slotIndex(), clean);
                    return true;
                } else {
                    if (!player.getInventory().add(clean)) {
                        player.drop(clean, false);
                    }
                    return true;
                }
            }
        }

        ItemStack clean = stack.copy();
        BaguetteCuriosData.removeSlotData(clean);
        if (!player.getInventory().add(clean)) {
            player.drop(clean, false);
        }
        return true;
    }
}
