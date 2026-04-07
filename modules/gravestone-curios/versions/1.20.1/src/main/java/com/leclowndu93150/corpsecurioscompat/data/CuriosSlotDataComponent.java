package com.leclowndu93150.corpsecurioscompat.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class CuriosSlotDataComponent {
    public static final String CURIO_SLOT_TAG = "GravestoneCuriosSlot";
    public static final String CURIO_SLOT_TYPE = "SlotType";
    public static final String CURIO_SLOT_INDEX = "SlotIndex";
    public static final String WAS_EQUIPPED = "WasEquipped";
    public static final String IS_COSMETIC = "IsCosmetic";

    public static class CurioSlotData {
        private final String slotType;
        private final int slotIndex;
        private final boolean wasEquipped;
        private final boolean isCosmetic;

        public CurioSlotData(String slotType, int slotIndex, boolean wasEquipped, boolean isCosmetic) {
            this.slotType = slotType;
            this.slotIndex = slotIndex;
            this.wasEquipped = wasEquipped;
            this.isCosmetic = isCosmetic;
        }

        public String slotType() { return slotType; }
        public int slotIndex() { return slotIndex; }
        public boolean wasEquipped() { return wasEquipped; }
        public boolean isCosmetic() { return isCosmetic; }

        public static CurioSlotData fromNBT(CompoundTag tag) {
            if (!tag.contains(CURIO_SLOT_TAG)) return null;
            CompoundTag slotData = tag.getCompound(CURIO_SLOT_TAG);
            return new CurioSlotData(
                slotData.getString(CURIO_SLOT_TYPE),
                slotData.getInt(CURIO_SLOT_INDEX),
                slotData.getBoolean(WAS_EQUIPPED),
                slotData.getBoolean(IS_COSMETIC)
            );
        }

        public void writeToNBT(ItemStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag slotData = new CompoundTag();
            slotData.putString(CURIO_SLOT_TYPE, slotType);
            slotData.putInt(CURIO_SLOT_INDEX, slotIndex);
            slotData.putBoolean(WAS_EQUIPPED, wasEquipped);
            slotData.putBoolean(IS_COSMETIC, isCosmetic);
            tag.put(CURIO_SLOT_TAG, slotData);
        }
    }

    public static CurioSlotData getCurioSlotData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        return CurioSlotData.fromNBT(tag);
    }

    public static void setCurioSlotData(ItemStack stack, String slotType, int slotIndex, boolean wasEquipped, boolean isCosmetic) {
        new CurioSlotData(slotType, slotIndex, wasEquipped, isCosmetic).writeToNBT(stack);
    }

    public static void removeCurioSlotData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(CURIO_SLOT_TAG)) {
            tag.remove(CURIO_SLOT_TAG);
            if (tag.isEmpty()) stack.setTag(null);
        }
    }
}
