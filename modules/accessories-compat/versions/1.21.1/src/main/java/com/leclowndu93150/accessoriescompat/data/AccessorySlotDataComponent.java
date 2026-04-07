package com.leclowndu93150.accessoriescompat.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import com.leclowndu93150.accessoriescompat.Main;

public class AccessorySlotDataComponent {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Main.MODID);

    public record AccessorySlotData(String slotName, int slotIndex, boolean wasEquipped) {}

    public static final Codec<AccessorySlotData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("slotName").forGetter(AccessorySlotData::slotName),
                    Codec.INT.fieldOf("slotIndex").forGetter(AccessorySlotData::slotIndex),
                    Codec.BOOL.fieldOf("wasEquipped").forGetter(AccessorySlotData::wasEquipped)
            ).apply(instance, AccessorySlotData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AccessorySlotData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AccessorySlotData::slotName,
            ByteBufCodecs.VAR_INT, AccessorySlotData::slotIndex,
            ByteBufCodecs.BOOL, AccessorySlotData::wasEquipped,
            AccessorySlotData::new);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessorySlotData>> ACCESSORY_SLOT_DATA =
            DATA_COMPONENTS.register("accessory_slot_data", () ->
                    DataComponentType.<AccessorySlotData>builder()
                            .persistent(CODEC)
                            .networkSynchronized(STREAM_CODEC)
                            .build());

    public static AccessorySlotData getSlotData(net.minecraft.world.item.ItemStack stack) {
        return stack.get(ACCESSORY_SLOT_DATA.get());
    }

    public static void setSlotData(net.minecraft.world.item.ItemStack stack, String slotName, int slotIndex) {
        stack.set(ACCESSORY_SLOT_DATA.get(), new AccessorySlotData(slotName, slotIndex, true));
    }

    public static void removeSlotData(net.minecraft.world.item.ItemStack stack) {
        stack.remove(ACCESSORY_SLOT_DATA.get());
    }
}
