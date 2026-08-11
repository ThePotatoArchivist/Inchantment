package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.PenchantmentDefinition;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public class PenchantRegistries {

    public static final ResourceKey<Registry<PenchantmentDefinition>> PENCHANTMENT_DEFINITION = ResourceKey.createRegistryKey(Penchant.id("definition"));

    public static void init() {
        DynamicRegistries.registerSynced(PENCHANTMENT_DEFINITION, PenchantmentDefinition.CODEC);

        DynamicRegistrySetupCallback.EVENT.register(registryView -> {
            registryView.registerEntryAdded(Registries.ENCHANTMENT, (rawId, id, enchantment) -> {
                var definitions = registryView.getOptional(PENCHANTMENT_DEFINITION).orElseThrow();
                if (definitions.containsKey(id)) return;
                Registry.register(definitions, id, PenchantmentDefinition.createFallback(enchantment));
            });
        });
    }
}
