package archives.tater.penchant.network;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.PenchantmentDefinition;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import io.netty.buffer.ByteBuf;

import java.util.List;

public record PenchantmentDefinitionsPayload(
        List<PenchantmentDefinition> definitions
) implements CustomPacketPayload {

    public static final Type<PenchantmentDefinitionsPayload> TYPE = new Type<>(Penchant.id("definitions"));
    public static final StreamCodec<ByteBuf, PenchantmentDefinitionsPayload> CODEC = PenchantmentDefinition.STREAM_CODEC.apply(ByteBufCodecs.list()).map(PenchantmentDefinitionsPayload::new, PenchantmentDefinitionsPayload::definitions);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
