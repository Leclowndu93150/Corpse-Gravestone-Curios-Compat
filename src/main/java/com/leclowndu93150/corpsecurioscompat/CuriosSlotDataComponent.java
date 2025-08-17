package com.leclowndu93150.corpsecurioscompat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class CuriosSlotDataComponent {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Main.MODID);

    public record CurioSlotData(String slotType, int slotIndex, boolean wasEquipped, boolean isCosmetic) {}

    public static final Codec<CurioSlotData> CURIO_SLOT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("slotType").forGetter(CurioSlotData::slotType),
                    Codec.INT.fieldOf("slotIndex").forGetter(CurioSlotData::slotIndex),
                    Codec.BOOL.fieldOf("wasEquipped").forGetter(CurioSlotData::wasEquipped),
                    Codec.BOOL.optionalFieldOf("isCosmetic", false).forGetter(CurioSlotData::isCosmetic)
            ).apply(instance, CurioSlotData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CurioSlotData> CURIO_SLOT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CurioSlotData::slotType,
            ByteBufCodecs.VAR_INT, CurioSlotData::slotIndex,
            ByteBufCodecs.BOOL, CurioSlotData::wasEquipped,
            ByteBufCodecs.BOOL, CurioSlotData::isCosmetic,
            CurioSlotData::new);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CurioSlotData>> CURIO_SLOT_DATA =
            DATA_COMPONENTS.register("curio_slot_data", () ->
                    DataComponentType.<CurioSlotData>builder()
                            .persistent(CURIO_SLOT_CODEC)
                            .networkSynchronized(CURIO_SLOT_STREAM_CODEC)
                            .build());
}