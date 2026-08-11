package archives.tater.penchant;

import archives.tater.penchant.network.PenchantmentDefinitionsPayload;
import archives.tater.penchant.registry.PenchantRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import com.google.common.collect.Streams;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

    public static PenchantmentDefinition createFallback(Holder<Enchantment> enchantment) {
        return new PenchantmentDefinition(
                enchantment.value().getAnvilCost(),
                max(2 * enchantment.value().getMinCost(1) - 5, 0),
                enchantment.value().definition().maxCost()
        );
    }

    private static final Map<Holder<Enchantment>, PenchantmentDefinition> CACHE = new WeakHashMap<>();

    public static void buildCache(HolderLookup.Provider registries) {
        var definitions = registries.lookupOrThrow(PenchantRegistries.PENCHANTMENT_DEFINITION);
        registries.lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(holder ->
                CACHE.put(holder, definitions.get(keyOf(holder.key()))
                        .map(Holder::value)
                        .orElseGet(() -> createFallback(holder)))
        );
    }

    public static PenchantmentDefinitionsPayload createPayload(HolderLookup.Provider registries) {
        return new PenchantmentDefinitionsPayload(
                registries.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                        .map(enchantment -> requireNonNull(CACHE.get(enchantment)))
                        .toList()
        );
    }

    public static void setReceivedCache(List<PenchantmentDefinition> definitions, HolderLookup.Provider registries) {
        Streams.forEachPair(
                registries.lookupOrThrow(Registries.ENCHANTMENT).listElements(),
                definitions.stream(),
                CACHE::put
        );
    }

    public static PenchantmentDefinition getDefinition(Holder<Enchantment> enchantment) {
        return requireNonNull(CACHE.get(enchantment));
    }
}
