package archives.tater.penchant;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Penchant implements ModInitializer {
	public static final String MOD_ID = "penchant";

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation id(String path) {
        return id(MOD_ID, path);
    }

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceLocation DURABILITY_REWORK = Penchant.id("durability_rework");

    private static <T> DataComponentType<@NotNull T> registerComponent(String path, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, @NotNull T> streamCodec, boolean cache/*, boolean ignoreSwapAnimation*/) {
        var type = DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec);
        if (cache) type.cacheEncoding();
//        if (ignoreSwapAnimation) type.ignoreSwapAnimation();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id(path), type.build());
    }

    public static final DataComponentType<@NotNull EnchantmentProgress> ENCHANTMENT_PROGRESS = registerComponent(
            "enchantment_progress",
            EnchantmentProgress.CODEC,
            EnchantmentProgress.STREAM_CODEC,
            true//,
//            true
    );

    public static Component getName(Holder<@NotNull Enchantment> enchantment) {
        return ComponentUtils.mergeStyles(
                enchantment.value().description().copy(),
                Style.EMPTY.withColor(enchantment.is(EnchantmentTags.CURSE)
                        ? ChatFormatting.RED
                        : ChatFormatting.GRAY
                )
        );
    }

	@Override
	public void onInitialize() {
        ResourceManagerHelper.registerBuiltinResourcePack(
                DURABILITY_REWORK,
                FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
                Component.translatable(DURABILITY_REWORK.toLanguageKey("dataPack", "name")),
                ResourcePackActivationType.DEFAULT_ENABLED
        );
	}
}
