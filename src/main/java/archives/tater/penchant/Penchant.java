package archives.tater.penchant;

import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.network.EnchantPayload;
import archives.tater.penchant.registry.*;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializers;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.max;

public class Penchant implements ModInitializer {
	public static final String MOD_ID = "penchant";
    public static final StreamCodec<ByteBuf, Enchantment.Cost> COST_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, Enchantment.Cost::base,
            ByteBufCodecs.INT, Enchantment.Cost::perLevelAboveFirst,
            Enchantment.Cost::new
    );

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }

    public static final Identifier FALLBACK_PARAMETERS = id("fallback_parameters");

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SuppressWarnings("UnstableApiUsage")
    @Override
	public void onInitialize() {
        PenchantFlag.init();
        PenchantComponents.init();
        PenchantEnchantments.init();
        PenchantMenus.init();
        PenchantAdvancements.init();
        PenchantModules.init();
        LootModification.init();

        PayloadTypeRegistry.serverboundPlay().register(EnchantPayload.TYPE, EnchantPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(EnchantPayload.TYPE, (payload, context) -> {
            if (!(context.player().containerMenu instanceof PenchantmentMenu menu)) {
                LOGGER.warn("Received enchant payload but enchantment menu was not open");
                return;
            }
            menu.handleEnchant(payload.enchantment());
        });

        FabricDataComponentInitializers.registerInitializer(FALLBACK_PARAMETERS, context -> context.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(enchantment -> {
            var builder = context.builder(enchantment.key());
            builder.getOrCreate(PenchantComponents.EXPERIENCE_COST, enchantment.value()::getAnvilCost);
            builder.getOrCreate(PenchantComponents.BOOK_REQUIREMENT, () -> max(2 * enchantment.value().getMinCost(1) - 5, 0));
            builder.getOrCreate(PenchantComponents.PROGRESS_COST_FACTOR, enchantment.value().definition()::maxCost);
        }));
        FabricDataComponentInitializers.addInitializerOrdering(FALLBACK_PARAMETERS, FabricDataComponentInitializers.DATA_HOLDER_COMPONENTS);
    }
}
