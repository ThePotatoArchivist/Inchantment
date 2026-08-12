package archives.tater.penchant.definition;

import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import io.netty.buffer.ByteBuf;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public record PenchantmentDefinition(
        int experienceCost,
        int bookRequirement,
        Enchantment.Cost progressCostFactor
) {
    public static final Codec<PenchantmentDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("experience_cost").forGetter(PenchantmentDefinition::experienceCost),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("book_requirement").forGetter(PenchantmentDefinition::bookRequirement),
            Enchantment.Cost.CODEC.fieldOf("progress_cost_factor").forGetter(PenchantmentDefinition::progressCostFactor)
    ).apply(instance, PenchantmentDefinition::new));

    public static final StreamCodec<ByteBuf, Enchantment.Cost> COST_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, Enchantment.Cost::base,
            ByteBufCodecs.INT, Enchantment.Cost::perLevelAboveFirst,
            Enchantment.Cost::new
    );

    public static final StreamCodec<ByteBuf, PenchantmentDefinition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PenchantmentDefinition::experienceCost,
            ByteBufCodecs.INT, PenchantmentDefinition::bookRequirement,
            COST_STREAM_CODEC, PenchantmentDefinition::progressCostFactor,
            PenchantmentDefinition::new
    );

    public int getProgressCostFactor(int targetLevel) {
        return max(progressCostFactor.calculate(targetLevel), 1);
    }

    public static ResourceKey<PenchantmentDefinition> keyOf(ResourceKey<Enchantment> enchantment) {
        return ResourceKey.create(PenchantRegistries.PENCHANTMENT_DEFINITION, enchantment.identifier());
    }

    public static PenchantmentDefinition getDefinition(Holder<Enchantment> enchantment) {
        return requireNonNull(enchantment.components().get(PenchantComponents.PENCHANTMENT_DEFINITION));
    }
}
