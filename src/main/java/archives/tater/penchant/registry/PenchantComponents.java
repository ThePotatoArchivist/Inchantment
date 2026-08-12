package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.component.GenericInitializerContext;
import archives.tater.penchant.component.RandomEnchantment;
import archives.tater.penchant.definition.PenchantmentDefinition;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

import org.jspecify.annotations.Nullable;

public class PenchantComponents {
    private static <T> DataComponentType<T> register(String path, @Nullable Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, boolean cache) {
        return register(path, codec, streamCodec, cache, false);
    }

    private static <T> DataComponentType<T> register(String path, @Nullable Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, boolean cache, boolean ignoreSwapAnimation) {
        var type = DataComponentType.<T>builder();
        if (codec != null) type.persistent(codec);
        if (streamCodec != null) type.networkSynchronized(streamCodec);
        if (cache) type.cacheEncoding();
        if (ignoreSwapAnimation) type.ignoreSwapAnimation();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Penchant.id(path), type.build());
    }

    public static final DataComponentType<EnchantmentProgress> ENCHANTMENT_PROGRESS = register(
            "enchantment_progress",
            EnchantmentProgress.CODEC,
            EnchantmentProgress.STREAM_CODEC,
            true,
            true
    );

    public static final DataComponentType<Integer> ENCHANTMENT_PROGRESS_COST_FACTOR = register(
            "enchantment_progress_cost_factor",
            ExtraCodecs.NON_NEGATIVE_INT,
            ByteBufCodecs.INT,
            false
    );

    public static final DataComponentType<RandomEnchantment> RANDOM_ENCHANTMENT = register(
            "random_enchantment",
            RandomEnchantment.CODEC,
            RandomEnchantment.STREAM_CODEC,
            true
    );

    // Set on the minecraft:enchantment registry, not for use in ItemStacks
    public static final DataComponentType<PenchantmentDefinition> PENCHANTMENT_DEFINITION = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Penchant.id("definition"),
            DataComponentType.<PenchantmentDefinition>builder()
                    .persistent(PenchantmentDefinition.CODEC)
                    .build()
    );

    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(
                    item -> item.components().has(DataComponents.MAX_DAMAGE),
                    (builder, provider, item) -> {
                        if (!builder.contains(ENCHANTMENT_PROGRESS_COST_FACTOR))
                            builder.set(ENCHANTMENT_PROGRESS_COST_FACTOR, PenchantFallbackParams.calculate(provider, item.components().getOrDefault(DataComponents.MAX_DAMAGE, 0)));
                    }
            );
        });

        ((GenericInitializerContext) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).penchant$registerGenericInitializer(
                Registries.ENCHANTMENT, (components, context, key) -> {
                    components.set(PenchantComponents.PENCHANTMENT_DEFINITION, context.getOrThrow(ResourceKey.create(PenchantRegistries.PENCHANTMENT_DEFINITION, key.identifier())).value());
                });
    }
}
