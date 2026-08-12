package archives.tater.penchant;

import archives.tater.penchant.component.GenericInitializerContext;
import archives.tater.penchant.definition.PenchantFallbackParamsManager;
import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.network.EnchantPayload;
import archives.tater.penchant.registry.*;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Penchant implements ModInitializer {
	public static final String MOD_ID = "penchant";

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final PenchantFallbackParamsManager FALLBACK_PARAMS = new PenchantFallbackParamsManager();

    @Override
	public void onInitialize() {
        PenchantRegistries.init();
        PenchantFlag.init();
        PenchantComponents.init();
        PenchantEnchantments.init();
        PenchantMenus.init();
        PenchantAdvancements.init();
        PenchantModules.init();
        LootModification.init();

        PayloadTypeRegistry.serverboundPlay().register(EnchantPayload.TYPE, EnchantPayload.CODEC);

        ((GenericInitializerContext) BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).penchant$registerGenericInitializer(Registries.ENCHANTMENT, (components, context, key) -> {
            components.set(PenchantComponents.PENCHANTMENT_DEFINITION, context.getOrThrow(ResourceKey.create(PenchantRegistries.PENCHANTMENT_DEFINITION, key.identifier())).value());
        });

        ServerPlayNetworking.registerGlobalReceiver(EnchantPayload.TYPE, (payload, context) -> {
            if (!(context.player().containerMenu instanceof PenchantmentMenu menu)) {
                LOGGER.warn("Received enchant payload but enchantment menu was not open");
                return;
            }
            menu.handleEnchant(payload.enchantment());
        });

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(PenchantFallbackParamsManager.ID, FALLBACK_PARAMS);
    }
}
