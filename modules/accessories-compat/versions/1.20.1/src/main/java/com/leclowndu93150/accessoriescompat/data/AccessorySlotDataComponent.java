package com.leclowndu93150.accessoriescompat.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class AccessorySlotDataComponent {
    public static final String TAG_KEY = "AccessoriesCorpseSlot";
    public static final String SLOT_NAME = "SlotName";
    public static final String SLOT_INDEX = "SlotIndex";
    public static final String WAS_EQUIPPED = "WasEquipped";

    public static class AccessorySlotData {
        private final String slotName;
        private final int slotIndex;
        private final boolean wasEquipped;

        public AccessorySlotData(String slotName, int slotIndex, boolean wasEquipped) {
            this.slotName = slotName;
            this.slotIndex = slotIndex;
            this.wasEquipped = wasEquipped;
        }

        public String slotName() { return slotName; }
        public int slotIndex() { return slotIndex; }
        public boolean wasEquipped() { return wasEquipped; }
    }

    public static AccessorySlotData getSlotData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_KEY)) return null;
        CompoundTag data = tag.getCompound(TAG_KEY);
        return new AccessorySlotData(data.getString(SLOT_NAME), data.getInt(SLOT_INDEX), data.getBoolean(WAS_EQUIPPED));
    }

    public static void setSlotData(ItemStack stack, String slotName, int slotIndex) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag data = new CompoundTag();
        data.putString(SLOT_NAME, slotName);
        data.putInt(SLOT_INDEX, slotIndex);
        data.putBoolean(WAS_EQUIPPED, true);
        tag.put(TAG_KEY, data);
    }

    public static void removeSlotData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_KEY)) {
            tag.remove(TAG_KEY);
            if (tag.isEmpty()) stack.setTag(null);
        }
    }
}
